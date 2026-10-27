package com.tecsup.lab10.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tecsup.lab10.data.SerieApiService
import com.tecsup.lab10.data.SerieModel
import kotlinx.coroutines.delay

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    val listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }
    LaunchedEffect(Unit) {
        val listado = servicio.selectSeries()
        listaSeries.clear()
        listado.forEach { listaSeries.add(it) }
    }
    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = "SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text(text = "Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            }
        }
        items(listaSeries) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id ?: 0}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                IconButton(
                    onClick = {
                        navController.navigate("serieVer/${item.id ?: 0}")
                        Log.e("SERIE-VER", "ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver", modifier = Modifier.align(Alignment.CenterVertically))
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id ?: 0}")
                        Log.e("SERIE-DEL", "ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Ver", modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    var id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf<String?>("") }
    var release_date by remember { mutableStateOf<String?>("") }
    var rating by remember { mutableStateOf<String?>("") }
    var category by remember { mutableStateOf<String?>("") }
    var grabar by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    if (id != 0) {
        LaunchedEffect(Unit) {
            try {
                val objSerie = servicio.selectSerie(id.toString())
                delay(100)
                name = objSerie.body()?.name
                release_date = objSerie.body()?.release_date
                rating = objSerie.body()?.rating.toString()
                category = objSerie.body()?.category
            } catch (e: Exception) {
                Log.e("SERIE-EDIT", "Error al cargar serie", e)
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = if (id == 0) "Autoincrementado" else id.toString(),
            onValueChange = { },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = name ?: "",
            onValueChange = { name = it },
            label = { Text("Name: ") },
            singleLine = true
        )
        TextField(
            value = release_date ?: "",
            onValueChange = { release_date = it },
            label = { Text("Release Date (YYYY-MM-DD):") },
            singleLine = true
        )
        TextField(
            value = rating ?: "",
            onValueChange = { rating = it },
            label = { Text("Rating:") },
            singleLine = true
        )
        TextField(
            value = category ?: "",
            onValueChange = { category = it },
            label = { Text("Category:") },
            singleLine = true
        )
        if (mensajeError != null) {
            Text(
                text = "Error de Django: $mensajeError",
                color = androidx.compose.ui.graphics.Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Button(
            onClick = {
                grabar = true
            }
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
    if (grabar) {
        LaunchedEffect(grabar) {
            try {
                mensajeError = null
                val objSerie = SerieModel(
                    id = if (id == 0) null else id,
                    name = (name ?: "").trim(),
                    release_date = (release_date ?: "").trim(),
                    rating = rating?.toIntOrNull() ?: 0,
                    category = (category ?: "").trim().lowercase()
                )
                val response = if (id == 0)
                    servicio.insertSerie(objSerie)
                else
                    servicio.updateSerie(id.toString(), objSerie)

                if (response.isSuccessful) {
                    Log.d("SERIE-SAVE", "Guardado exitoso: ${response.body()}")
                    grabar = false
                    navController.navigate("series")
                } else {
                    val err = response.errorBody()?.string()
                    Log.e("SERIE-SAVE", "Error Django (${response.code()}): $err")
                    mensajeError = err
                    grabar = false
                }
            } catch (e: Exception) {
                Log.e("SERIE-SAVE", "Excepción al guardar", e)
                mensajeError = e.localizedMessage
                grabar = false
            }
        }
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        borrar = true
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    navController.navigate("series")
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (borrar) {
        LaunchedEffect(Unit) {
            servicio.deleteSerie(id.toString())
            borrar = false
            navController.navigate("series")
        }
    }
}
@Composable
fun ScreenInicio() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bienvenido a Series App", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}