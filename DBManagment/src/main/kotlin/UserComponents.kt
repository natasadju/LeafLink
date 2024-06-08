import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val password: String,
    val __v: Int
)


fun fetchUsers(onResult: (List<User>?) -> Unit) {
    val request = Request.Builder()
        .url("http://172.211.85.100:3000/api/v1/users")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
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



fun addUser(user: User, onResult: (Boolean) -> Unit) {
    val requestData = mapOf(
        "email" to user.email,
        "password" to user.password,
        "username" to user.name
    )

    val jsonBody = gson.toJson(requestData)

    val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

    val request = Request.Builder()
        .url("http://172.211.85.100:3000/api/v1/users/register")
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
        .url("http://172.211.85.100:3000/api/v1/users/${user._id}")
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