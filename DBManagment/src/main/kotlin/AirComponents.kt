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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


data class AirData(
    val _id: String,
    val station: String,
    val pm10: String,
    val pm25: String,
    val so2: String,
    val co: String,
    val ozon: String,
    val no2: String,
    val benzen: String,
    val timestamp: String,
    val isFake: Boolean,
    val __v: Int
)

fun addScrapedAirData(item: AirData, onResult: (Boolean) -> Unit) {
    val requestBody = gson.toJson(item)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/air")
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


fun fetchAirData(onResult: (List<AirData>?) -> Unit) {
    val request = Request.Builder()
        .url("http://172.211.85.100:3000/air")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let { body ->
                try {
                    val airDataArray = gson.fromJson(body, JsonArray::class.java)
                    val airData: List<AirData> =
                        gson.fromJson(airDataArray, object : TypeToken<List<AirData>>() {}.type)
                    onResult(airData)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    onResult(null)
                }
            }
        }
    })
}

fun updateAir(airData: AirData, onResult: (Boolean) -> Unit) {
    val updateFields = mutableMapOf<String, Any>(
        "station" to airData.station,
        "pm10" to airData.pm10,
        "pm25" to airData.pm25,
        "so2" to airData.so2,
        "co" to airData.co,
        "ozon" to airData.ozon,
        "no2" to airData.no2,
        "benzen" to airData.benzen
    )

    val requestBody = gson.toJson(updateFields)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/air/${airData._id}")
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
fun AirDataGrid() {
    var airData by remember { mutableStateOf<List<AirData>?>(null) }
    val lazyGridState = rememberLazyGridState()
    var airBeingEdited by remember { mutableStateOf<AirData?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchAirData { fetchedData ->
            airData = fetchedData
        }
    }

    airData?.let { dataList ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dataList.size) { index ->
                AirCard(item = dataList[index]) {
                    airBeingEdited = it
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

    if (showEditDialog && airBeingEdited != null) {
        EditAirDialog(
            item = airBeingEdited!!,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedAir ->
                airData = airData?.map { if (it._id == updatedAir._id) updatedAir else it }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ScrapedAirCard(item: AirData) {
    var isAdding by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.station,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("PM10: ${item.pm10}")
            Text("PM2.5: ${item.pm25}")
            Text("SO2: ${item.so2}")
            Text("CO: ${item.co}")
            Text("Ozon: ${item.ozon}")
            Text("NO2: ${item.no2}")
            Text("Benzen: ${item.benzen}")
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
                    addScrapedAirData(item) { success ->
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
fun EditAirDialog(item: AirData, onDismiss: () -> Unit, onUpdate: (AirData) -> Unit) {
    var station by remember { mutableStateOf(item.station ?: "") }
    var pm10 by remember { mutableStateOf(item.pm10 ?: "") }
    var pm25 by remember { mutableStateOf(item.pm25 ?: "") }
    var so2 by remember { mutableStateOf(item.so2 ?: "") }
    var co by remember { mutableStateOf(item.co ?: "") }
    var ozon by remember { mutableStateOf(item.ozon ?: "") }
    var no2 by remember { mutableStateOf(item.no2 ?: "") }
    var benzen by remember { mutableStateOf(item.benzen ?: "") }
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
                Text(text = "Edit Air Data", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = station,
                    onValueChange = { station = it },
                    label = { Text("Station") }
                )
                TextField(
                    value = pm10,
                    onValueChange = { pm10 = it },
                    label = { Text("PM10") }
                )
                TextField(
                    value = pm25,
                    onValueChange = { pm25 = it },
                    label = { Text("PM2.5") }
                )
                TextField(
                    value = so2,
                    onValueChange = { so2 = it },
                    label = { Text("SO2") }
                )
                TextField(
                    value = co,
                    onValueChange = { co = it },
                    label = { Text("CO") }
                )
                TextField(
                    value = ozon,
                    onValueChange = { ozon = it },
                    label = { Text("Ozon") }
                )
                TextField(
                    value = no2,
                    onValueChange = { no2 = it },
                    label = { Text("NO2") }
                )
                TextField(
                    value = benzen,
                    onValueChange = { benzen = it },
                    label = { Text("Benzen") }
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
                            val updatedAir = item.copy(
                                station = station,
                                pm10 = pm10,
                                pm25 = pm25,
                                so2 = so2,
                                co = co,
                                ozon = ozon,
                                no2 = no2,
                                benzen = benzen
                            )
                            updateAir(updatedAir) { success ->
                                isUpdating = false
                                if (success) {
                                    onUpdate(updatedAir)

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
fun AirCard(item: AirData, onClick: (AirData) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val formattedDate = LocalDateTime.parse(item.timestamp, DateTimeFormatter.ISO_DATE_TIME).format(formatter)

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
                text = item.station,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("PM10: ${item.pm10}")
            Text("PM2.5: ${item.pm25}")
            Text("SO2: ${item.so2}")
            Text("CO: ${item.co}")
            Text("Ozon: ${item.ozon}")
            Text("NO2: ${item.no2}")
            Text("Benzen: ${item.benzen}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Timestamp: ${formattedDate}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AddAirScreen(onAirAdded: () -> Unit) {
    var station by remember { mutableStateOf("") }
    var pm10 by remember { mutableStateOf("") }
    var pm25 by remember { mutableStateOf("") }
    var so2 by remember { mutableStateOf("") }
    var co by remember { mutableStateOf("") }
    var ozon by remember { mutableStateOf("") }
    var no2 by remember { mutableStateOf("") }
    var benzen by remember { mutableStateOf("") }
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
            value = station,
            onValueChange = { station = it },
            label = { Text("Station") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = pm10,
            onValueChange = { pm10 = it },
            label = { Text("PM10") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = pm25,
            onValueChange = { pm25 = it },
            label = { Text("PM2.5") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = so2,
            onValueChange = { so2 = it },
            label = { Text("SO2") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = co,
            onValueChange = { co = it },
            label = { Text("CO") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = ozon,
            onValueChange = { ozon = it },
            label = { Text("Ozon") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = no2,
            onValueChange = { no2 = it },
            label = { Text("NO2") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = benzen,
            onValueChange = { benzen = it },
            label = { Text("Benzen") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isAdding = true
                val airData = AirData(
                    _id = UUID.randomUUID().toString(),
                    station = station,
                    pm10 = pm10,
                    pm25 = pm25,
                    so2 = so2,
                    co = co,
                    ozon = ozon,
                    no2 = no2,
                    benzen = benzen,
                    timestamp = System.currentTimeMillis().toString(),
                    isFake = false,
                    __v = 0
                )
                addScrapedAirData(airData) { success ->
                    isAdding = false
                    showMessage = true
                    if (success) {
                        onAirAdded()
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
                Text("Add Air Data")
            }
        }
        if (showMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAdding) "Adding..." else "Air data added!",
                color = if (isAdding) Color.Gray else Color.Green
            )
        }
    }
}
