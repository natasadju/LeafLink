import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.serpro69.kfaker.Faker
import java.time.LocalDateTime
import java.util.*

@Composable
fun GeneratorMenu() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("AirQuality") }
    var generatedData by remember { mutableStateOf(emptyList<Any>()) }
    var count by remember { mutableStateOf("10") }
    val options = listOf("AirQuality", "Pollen")

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Data to Generate:")
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
                        generatedData = emptyList()
                    }) {
                        Text(option)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = count,
            onValueChange = { count = it },
            label = { Text("Count") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val countInt = count.toIntOrNull() ?: 10
            generatedData = generateData(selectedOption, countInt)
        }) {
            Text("Generate Data")
        }
        Spacer(modifier = Modifier.height(16.dp))
        ScrapedDataGrid(generatedData, selectedOption)
    }
}


fun generateData(option: String, count: Int): List<Any> {
    return when (option) {
        "AirQuality" -> generateAirData(count)
        "Pollen" -> generatePollen(count)
        else -> emptyList()
    }
}

fun generateAirData(count: Int): List<AirData> {
    val faker = Faker()
    val stations = listOf("MB Titova", "MB Vrbanski")
    return List(count) {
        AirData(
            _id = faker.random.nextUUID(),
            station = stations.random(),
            pm10 = faker.random.nextInt(0, 20).toString(),
            pm25 = faker.random.nextInt(0, 15).toString(),
            so2 = "",
            co = "",
            ozon = faker.random.nextInt(0, 100).toString(),
            no2 = faker.random.nextInt(3, 40).toString(),
            benzen = String.format(Locale.US, "%.1f", faker.random.nextDouble()),
            timestamp = generateRandomTimestamp().toString(),
            isFake = true,
            __v = 0
        )
    }
}

fun generateRandomTimestamp(): LocalDateTime {
    val faker = Faker()
    val year = faker.random.nextInt(2017, LocalDateTime.now().year)
    val month = faker.random.nextInt(1, 12)
    val day = faker.random.nextInt(1, 28)
    val hour = faker.random.nextInt(0, 23)
    val minute = faker.random.nextInt(0, 59)
    val second = faker.random.nextInt(0, 59)

    return LocalDateTime.of(year, month, day, hour, minute, second)
}

fun generatePollen(count: Int): List<PollenItem> {
    val faker = Faker()
    val pollenTypes = listOf("Grasses", "Birch", "Olive Tree")
    return List(count) {
        PollenItem(
            _id = faker.random.nextUUID(),
            type = pollenTypes.random(),
            value = String.format(Locale.US, "%.1f", faker.random.nextDouble()),
            timestamp = generateRandomTimestamp().toString(),
            isFake = true,
            __v = 0
        )
    }
}