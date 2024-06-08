import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.*


data class Park(
    val _id: String,
    val name: String,
    val parkId: String,
    val __v: Int
)

fun fetchParks(onResult: (List<Park>?) -> Unit) {
    val request = Request.Builder()
        .url("http://172.211.85.100:3000/parks")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                val jsonObject = gson.fromJson(body, JsonObject::class.java)
                val parksArray = jsonObject.getAsJsonArray("parks")
                val parks: List<Park> = gson.fromJson(parksArray, object : TypeToken<List<Park>>() {}.type)
                onResult(parks)
            }
        }
    })
}


fun addPark(park: Park, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(mapOf("name" to park.name, "parkId" to park.parkId))
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/parks/parks/addparks")
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

fun updatePark(park: Park, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(mapOf("name" to park.name, "parkId" to park.parkId))
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/parks/${park._id}")
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
fun ParkGrid() {
    var parks by remember { mutableStateOf<List<Park>?>(null) }
    val lazyGridState = rememberLazyGridState()
    var parkBeingEdited by remember { mutableStateOf<Park?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchParks { fetchedParks ->
            parks = fetchedParks
        }
    }

    parks?.let { parkList ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(parkList.size) { index ->
                ParkCard(park = parkList[index]) {
                    parkBeingEdited = it
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

    if (showEditDialog && parkBeingEdited != null) {
        EditParkDialog(
            park = parkBeingEdited!!,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedPark ->
                parks = parks?.map { if (it._id == updatedPark._id) updatedPark else it }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditParkDialog(park: Park, onDismiss: () -> Unit, onUpdate: (Park) -> Unit) {
    var name by remember { mutableStateOf(park.name) }
    var parkId by remember { mutableStateOf(park.parkId) }
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
                Text(text = "Edit Park", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Park Name") }
                )
                TextField(
                    value = parkId,
                    onValueChange = { parkId = it },
                    label = { Text("Park ID") }
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
                            val updatedPark = park.copy(name = name, parkId = parkId)
                            updatePark(updatedPark) { success ->
                                isUpdating = false
                                if (success) {
                                    onUpdate(updatedPark)
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
fun ParkCard(park: Park, onClick: (Park) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable { onClick(park) },
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
                imageVector = Icons.Default.Forest,
                contentDescription = "Forest Icon",
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = park.name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ID: ${park.parkId}",
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun AddParkScreen(onParkAdded: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var parkId by remember { mutableStateOf("") }
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
            label = { Text("Park Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = parkId,
            onValueChange = { parkId = it },
            label = { Text("parkId") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isAdding = true
                val park = Park(
                    _id = UUID.randomUUID().toString(),
                    name = name,
                    parkId = parkId,
                    __v = 0
                )
                addPark(park) { success ->
                    isAdding = false
                    showMessage = true
                    if (success) {
                        onParkAdded()
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
                Text("Add Park")
            }
        }
        if (showMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAdding) "Adding..." else "Park added!",
                color = if (isAdding) Color.Gray else Color.Green
            )
        }
    }
}

