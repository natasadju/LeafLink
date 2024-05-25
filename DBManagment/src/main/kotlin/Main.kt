import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import java.util.*


//TODO: add date-time to airquality scraping data

data class Park(
    val _id: String,
    val name: String,
    val parkId: String,
    val __v: Int
)

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val password: String,
    val __v: Int
)

val client: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}

val gson = Gson()

fun fetchUsers(onResult: (List<User>?) -> Unit) {
    val request = Request.Builder()
        .url("http://localhost:3000/api/v1/users")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: java.io.IOException) {
            e.printStackTrace()
            onResult(null)
        }

        override fun onResponse(call: Call, response: Response) {
            response.body?.string().let { body ->
                val jsonObject = gson.fromJson(body, JsonObject::class.java)
                val usersArray = jsonObject.getAsJsonArray("users")
                val users: List<User> = gson.fromJson(usersArray, object : TypeToken<List<User>>() {}.type)
                onResult(users)
            }
        }
    })
}

fun fetchParks(onResult: (List<Park>?) -> Unit) {
    val request = Request.Builder()
        .url("http://localhost:3000/parks")
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
        .url("http://localhost:3000/parks/addparks")
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
        .url("http://localhost:3000/parks/${park._id}")
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

fun addUser(user: User, onResult: (Boolean) -> Unit) {
    val requestData = mapOf(
        "email" to user.email,
        "password" to user.password,
        "username" to user.name
    )

    val jsonBody = gson.toJson(requestData)

    val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/api/v1/register")
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


fun updateUser(user: User, onResult: (Boolean) -> Unit) {
    val updateFields = mutableMapOf<String, Any>(
        "name" to user.name,
        "email" to user.email
    )

    val requestBody = gson.toJson(updateFields)
        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://localhost:3000/api/v1/users/${user._id}")
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


data class ScrapedItem(
    val station: String,
    val pm10: String,
    val pm25: String,
    val so2: String,
    val co: String,
    val ozon: String,
    val no2: String,
    val benzen: String
)

fun scrapeData(option: String): List<ScrapedItem> {
    return when (option) {
        "AirQuality" -> scrapeAirQuality()
        else -> emptyList()
    }
}

fun scrapePollen() {
    //
}

inline fun <T> T.maybe(block: T.() -> T?): T? = try {
    block()
} catch (e: Exception) {
    null
}

fun scrapeAirQuality(): List<ScrapedItem> {
    val scrapedItems = mutableListOf<ScrapedItem>()

    skrape(HttpFetcher) {
        request {
            url = "https://www.arso.gov.si/zrak/kakovost%20zraka/podatki/dnevne_koncentracije.html"
        }

        response {
            htmlDocument {
                fun processTable(tableIndex: Int) {
                    val tables = findAll("table.online")
                    if (tableIndex < tables.size) {
                        val table = tables[tableIndex]
                        table.findAll("tr").drop(3).forEach { row ->
                            try {
                                val stationCell = row.maybe { findFirst(".onlineimena") }
                                if (stationCell != null &&
                                    (stationCell.text.contains("MB Vrbanski") || stationCell.text.contains("MB Titova"))
                                ) {
                                    val station = stationCell.text
                                    val cells = row.findAll(".onlinedesno")
                                    if (cells.size >= 7) {
                                        val pm10 = cells[0].text
                                        val pm25 = cells[1].text
                                        val so2 = cells[2].text
                                        val co = cells[3].text
                                        val ozon = cells[4].text
                                        val no2 = cells[5].text
                                        val benzen = cells[6].text

                                        scrapedItems.add(
                                            ScrapedItem(
                                                station = station,
                                                pm10 = pm10,
                                                pm25 = pm25,
                                                so2 = so2,
                                                co = co,
                                                ozon = ozon,
                                                no2 = no2,
                                                benzen = benzen
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                println("Error: ${e.message}")
                            }
                        }
                    } else {
                        println("Table at index $tableIndex not found")
                    }
                }

                processTable(0)
            }
        }
    }

    return scrapedItems
}


@Composable
@Preview
fun App() {
    var selectedButton by remember { mutableStateOf("Add park") }
    var selectedScreen by remember { mutableStateOf("Add park") }

    Row(modifier = Modifier.fillMaxSize()) {
        Sidebar(selectedButton) { button ->
            selectedButton = button
            selectedScreen = button
        }
        when (selectedScreen) {
            "Add park" -> AddParkScreen {
            }

            "Add user" -> AddUserScreen {}

            "Parks" -> ParkGrid()
            "Users" -> UserGrid()
            "Scraper" -> ScraperMenu()
            else -> {}
        }
    }
}

@Composable
fun ScraperMenu() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("AirQuality") }
    var scrapedData by remember { mutableStateOf(emptyList<ScrapedItem>()) }
    val options = listOf("AirQuality", "Pollen")

    Column(
        modifier = Modifier.padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Data to Scrape:")
        Box {
            Button(
                onClick = { expanded = true },
                modifier = Modifier
                    .background(Color.Transparent)
                    .align(Alignment.Center)
            ) {
                Text(selectedOption)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        selectedOption = option
                        expanded = false
                        scrapedData = scrapeData(option)
                    }) {
                        Text(option)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ScrapedDataGrid(scrapedData)
    }
}


@Composable
fun ScrapedDataGrid(scrapedData: List<ScrapedItem>) {
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        state = lazyGridState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(scrapedData.size) { index ->
            ScrapedAirCard(scrapedData[index])
        }
    }
}


@Composable
fun ScrapedAirCard(item: ScrapedItem) {
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
        }
    }
}


@Composable
fun UserGrid() {
    var users by remember { mutableStateOf<List<User>?>(null) }
    val lazyGridState = rememberLazyGridState()
    var userBeingEdited by remember { mutableStateOf<User?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        fetchUsers { fetchedUsers ->
            users = fetchedUsers
        }
    }

    users?.let { userList ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            state = lazyGridState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(userList.size) { index ->
                UserCard(user = userList[index]) {
                    userBeingEdited = it
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

    if (showEditDialog && userBeingEdited != null) {
        EditUserDialog(
            user = userBeingEdited!!,
            onDismiss = { showEditDialog = false },
            onUpdate = { updatedUser ->
                users = users?.map { if (it._id == updatedUser._id) updatedUser else it }
                showEditDialog = false
            }
        )
    }
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
fun EditUserDialog(user: User, onDismiss: () -> Unit, onUpdate: (User) -> Unit) {
    var username by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
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
                Text(text = "Edit User", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Name") }
                )
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
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
                            val updatedUser = user.copy(name = username, email = email)
                            updateUser(updatedUser) { success ->
                                isUpdating = false
                                if (success) {
                                    onUpdate(updatedUser)
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
fun UserCard(user: User, onClick: (User) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clickable { onClick(user) },
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
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                modifier = Modifier.size(50.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = user.name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "E-mail: ${user.email}",
                textAlign = TextAlign.Center
            )
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

@Composable
fun AddUserScreen(onUserAdded: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
            value = username,
            onValueChange = { username = it },
            label = { Text("User Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                isAdding = true
                val user = User(
                    _id = UUID.randomUUID().toString(),
                    name = username,
                    email = email,
                    password = password,
                    __v = 0
                )
                addUser(user) { success ->
                    isAdding = false
                    showMessage = true
                    if (success) {
                        onUserAdded()
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
                Text("Add User")
            }
        }
        if (showMessage) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isAdding) "Adding..." else "User added!",
                color = if (isAdding) Color.Gray else Color.Green
            )
        }
    }
}


@Composable
fun Sidebar(selectedButton: String, onButtonSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        Divider()
        SidebarButton(
            text = "Add park",
            isSelected = selectedButton == "Add park",
            onClick = { onButtonSelected("Add park") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Parks",
            isSelected = selectedButton == "Parks",
            onClick = { onButtonSelected("Parks") },
            icon = Icons.Default.Menu
        )
        Divider()
        SidebarButton(
            text = "Add user",
            isSelected = selectedButton == "Add user",
            onClick = { onButtonSelected("Add user") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Users",
            isSelected = selectedButton == "Users",
            onClick = { onButtonSelected("Users") },
            icon = Icons.Default.Menu
        )
        Divider()
        SidebarButton(
            text = "Scraper",
            isSelected = selectedButton == "Scraper",
            onClick = { onButtonSelected("Scraper") },
            icon = Icons.Default.Share
        )
        SidebarButton(
            text = "Generator",
            isSelected = selectedButton == "Generator",
            onClick = { onButtonSelected("Generator") },
            icon = Icons.Default.Build
        )
        Spacer(modifier = Modifier.weight(1f))
        SidebarButton(
            text = "About",
            isSelected = selectedButton == "About",
            onClick = { onButtonSelected("About") },
            icon = Icons.Default.Info
        )
    }
}

@Composable
fun SidebarButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface
    val contentColor = if (isSelected) Color.White else Color.Black

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = contentColor,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Database manager") {
        MaterialTheme {
            Box {
                App()
            }
        }
    }
}
