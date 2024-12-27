import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.Gson
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor

val client: OkHttpClient by lazy {
    val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
}

val gson = Gson()

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
            "Add park" -> AddParkScreen {}
            "Add user" -> AddUserScreen {}
            "Parks" -> ParkGrid()
            "Users" -> UserGrid()
            "Air Quality" -> AirDataGrid()
            "Add Air Data" -> AddAirScreen {}
            "Pollen" -> PollenGrid()
            "Add Pollen Data" -> AddPollenScreen {}
            "Scraper" -> ScraperMenu()
            "Generator" -> GeneratorMenu()
            "Add Event" -> AddEventScreen {}
            "Events" -> EventGrid()
            "About" -> AboutTab()
            else -> {}
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
            icon = Icons.Default.Forest
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
            icon = Icons.Default.Person
        )
        Divider()
        SidebarButton(
            text = "Add Event",
            isSelected = selectedButton == "Add Event",
            onClick = { onButtonSelected("Add Event") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Events",
            isSelected = selectedButton == "Events",
            onClick = { onButtonSelected("Events") },
            icon = Icons.Default.Event
        )
        Divider()
        SidebarButton(
            text = "Add Air Data",
            isSelected = selectedButton == "Add Air Data",
            onClick = { onButtonSelected("Add Air Data") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Air Quality",
            isSelected = selectedButton == "Air Quality",
            onClick = { onButtonSelected("Air Quality") },
            icon = Icons.Default.Air
        )
        Divider()
        SidebarButton(
            text = "Add Pollen",
            isSelected = selectedButton == "Add Pollen Data",
            onClick = { onButtonSelected("Add Pollen Data") },
            icon = Icons.Default.Add
        )
        SidebarButton(
            text = "Pollen",
            isSelected = selectedButton == "Pollen",
            onClick = { onButtonSelected("Pollen") },
            icon = Icons.Default.Grass
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

@Composable
fun AboutTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About Icon",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "About the Database Management App",
                style = MaterialTheme.typography.h4.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Text(
            text = "This database management application is designed for the LeafLink site. It enables efficient management of the site's database, allowing users to perform tasks such as adding, editing, and viewing various types of data. Additionally, the app can scrape data from the web and generate synthetic data for testing purposes.",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "Key Features:",
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FeatureItem(text = "Add, Edit, and View Database Content: Users can easily manage the database on various data entries such as parks, users, events, air quality, and pollen data.")
        FeatureItem(text = "Data Scraping: The app uses the skrape{it} library to scrape air quality data from HTML sites, providing users with updated information.")
        FeatureItem(text = "Fake Data Generation: For testing purposes, the app can generate synthetic data that mimics real-world data.")
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = "Team Icon",
                tint = MaterialTheme.colors.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Developed by Project Group Alters",
                style = MaterialTheme.typography.h5.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Text(
            text = "This project was developed by the dedicated team of Alters. The team members include:",
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TeamMember(name = "Teodora Zečević")
        TeamMember(name = "Nataša Đurić")
        TeamMember(name = "Gligor Gligorov")
        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "The Alters team is committed to enhancing community engagement and environmental awareness through innovative digital solutions like this application.",
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun FeatureItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.body1,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun TeamMember(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.body1,
        modifier = Modifier.padding(bottom = 4.dp)
    )
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
