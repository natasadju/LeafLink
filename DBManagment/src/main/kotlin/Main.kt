import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
@Preview
fun App() {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        var selectedButton by remember { mutableStateOf("Add user") }

        Sidebar(selectedButton) { button ->
            selectedButton = button
        }
        Content()
    }
}

fun DataGrid() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

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

@Composable
fun Content() {
    var text by remember { mutableStateOf("Hello, World!") }
    Text(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        text = text
    )
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Database manager") {
        MaterialTheme {
            Scaffold(
                drawerContent = {
                    Sidebar("Add user") {}
                },
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                App()
            }
        }
    }
}
