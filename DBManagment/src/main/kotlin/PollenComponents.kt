import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    val __v: Int
)

fun fetchPollenData(onResult: (List<PollenItem>?) -> Unit) {
    val request = Request.Builder()
        .url("http://localhost:3000/pollen")
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
        .url("http://localhost:3000/pollen")
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

    LaunchedEffect(Unit) {
        fetchPollenData { fetchedData ->
            pollenData = fetchedData
        }
    }

    pollenData?.let { dataList ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dataList) { item ->
                PollenCard(item = item)
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
