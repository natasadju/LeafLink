import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class Event(
    val _id: String,
    val name: String,
    val parkId: String,
    val description: String,
    val date: String,
    val __v: Int
)

fun fetchEvents(onResult: (List<Event>?) -> Unit) {
    val request = Request.Builder()
        .url("http://172.211.85.100:3000/events")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                try {
                    val events: List<Event> = gson.fromJson(body, object : TypeToken<List<Event>>() {}.type)
                    onResult(events)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onResult(null)
                }
            }
        }
    })
}

fun addEvent(event: Event, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(event)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/events")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(false)
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(response.isSuccessful)
        }
    })
}

fun updateEvent(event: Event, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(event)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/events/${event._id}")
        .put(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(false)
        }

        override fun onResponse(call: Call, response: Response) {
            onResult(response.isSuccessful)
        }
    })
}

@Composable
fun EventGrid() {
    var events by remember { mutableStateOf<List<Event>?>(null) }
    val lazyGridState = rememberLazyGridState()
    var eventBeingEdited by remember { mutableStateOf<Event?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchEvents { fetchedEvents ->
            events = fetchedEvents
        }
    }

    events?.let { eventList ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(eventList.size) { index ->
                EventCard(event = eventList[index]) {
                    eventBeingEdited = it
                    showEditDialog = true
                }
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }

    if (showEditDialog && eventBeingEdited != null) {
        EditEventDialog(
            event = eventBeingEdited!!,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedEvent ->
                events = events?.map { if (it._id == updatedEvent._id) updatedEvent else it }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditEventDialog(event: Event, onDismiss: () -> Unit, onUpdate: (Event) -> Unit) {
    var name by remember { mutableStateOf(event.name) }
    var parkId by remember { mutableStateOf(event.parkId) }
    var description by remember { mutableStateOf(event.description) }
    var date by remember { mutableStateOf(event.date) }
    var isUpdating by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Edit Event", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") }
                )
                TextField(
                    value = parkId,
                    onValueChange = { parkId = it },
                    label = { Text("Park ID") }
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                TextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isUpdating = true
                            val updatedEvent =
                                event.copy(name = name, parkId = parkId, description = description, date = date)
                            updateEvent(updatedEvent) { success ->
                                isUpdating = false
                                if (success) {
                                    onUpdate(updatedEvent)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event, onClick: (Event) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val formattedDate = LocalDateTime.parse(event.date, DateTimeFormatter.ISO_DATE_TIME).format(formatter)

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxHeight()
            .clickable { onClick(event) },
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = "Event Icon",
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.name,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Park ID: ${event.parkId}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Description: ${event.description}",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Date: $formattedDate",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AddEventScreen(onEventAdded: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var parkId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Event Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = parkId,
            onValueChange = { parkId = it },
            label = { Text("Park ID") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isAdding = true
                val event = Event(
                    _id = UUID.randomUUID().toString(),
                    name = name,
                    parkId = parkId,
                    description = description,
                    __v = 0,
                    date = date
                )
                addEvent(event) { success ->
                    isAdding = false
                    showMessage = true
                    if (success) {
                        onEventAdded()
                    }
                }
            },
            enabled = !isAdding
        ) {
            if (isAdding) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Add Event")
            }
        }
        if (showMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAdding) "Adding..." else "Event added!",
                color = if (isAdding) Color.Gray else Color.Green
            )
        }
    }
}
