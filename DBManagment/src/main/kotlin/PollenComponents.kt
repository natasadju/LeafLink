import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.JsonArray
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.*

data class PollenItem(
    val _id: String,
    val type: String,
    val value: String,
    val timestamp: String,
    val isFake: Boolean,
    val __v: Int
)

fun fetchPollenData(onResult: (List<PollenItem>?) -> Unit) {
    val request = Request.Builder()
        .url("http://172.211.85.100:3000/pollen")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                try {
                    val pollenDataArray = gson.fromJson(body, JsonArray::class.java)
                    val pollenData: List<PollenItem> =
                        gson.fromJson(pollenDataArray, object : TypeToken<List<PollenItem>>() {}.type)
                    onResult(pollenData)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    onResult(null)
                }
            }
        }
    })
}

fun addPollenData(item: PollenItem, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(item)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/pollen")
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

fun updatePollenData(item: PollenItem, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(item)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/pollen/${item._id}")
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
fun PollenCard(item: PollenItem) {
    var isAdding by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.type,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Value: ${item.value}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.timestamp,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    isAdding = true
                    addPollenData(item) { success ->
                        isAdding = false
                        showMessage = true
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
                    Text("Add to Database")
                }
            }
            if (showMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAdding) "Adding..." else "Data added!",
                    color = if (isAdding) Color.Gray else Color.Green
                )
            }
        }
    }
}

@Composable
fun AddPollenScreen(onPollenAdded: () -> Unit) {
    var type by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
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
            value = type,
            onValueChange = { type = it },
            label = { Text("Type") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Value") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isAdding = true
                val pollen = PollenItem(
                    _id = UUID.randomUUID().toString(),
                    type = type,
                    value = value,
                    timestamp = System.currentTimeMillis().toString(),
                    isFake = false,
                    __v = 0
                )
                addPollenData(pollen) { success ->
                    isAdding = false
                    showMessage = true
                    if (success) {
                        onPollenAdded()
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
                Text("Add Pollen Data")
            }
        }
        if (showMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAdding) "Adding..." else "Pollen data added!",
                color = if (isAdding) Color.Gray else Color.Green
            )
        }
    }
}

@Composable
fun PollenGrid() {
    var pollenData by remember { mutableStateOf<List<PollenItem>?>(null) }
    val lazyGridState = rememberLazyGridState()
    var pollenBeingEdited by remember { mutableStateOf<PollenItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchPollenData { fetchedData ->
            pollenData = fetchedData
        }
    }

    pollenData?.let { dataList ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dataList.size) { index ->
                PollenCard(item = dataList[index]) {
                    pollenBeingEdited = it
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

    if (showEditDialog && pollenBeingEdited != null) {
        EditPollenDialog(
            item = pollenBeingEdited!!,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedPollen ->
                pollenData = pollenData?.map { if (it._id == updatedPollen._id) updatedPollen else it }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun PollenCard(item: PollenItem, onClick: (PollenItem) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick(item) },
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.type,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Value: ${item.value}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Timestamp: ${item.timestamp}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EditPollenDialog(item: PollenItem, onDismiss: () -> Unit, onUpdate: (PollenItem) -> Unit) {
    var type by remember { mutableStateOf(item.type) }
    var value by remember { mutableStateOf(item.value) }
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
                Text(text = "Edit Pollen Data", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") }
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
                            val updatedPollen = item.copy(
                                type = type,
                                value = value
                            )
                            updatePollenData(updatedPollen) { success ->
                                isUpdating = false
                                if (success) {
                                    onUpdate(updatedPollen)
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