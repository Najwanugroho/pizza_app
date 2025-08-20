package com.vdi.pizzaapp.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vdi.pizzaapp.data.Pizza
import com.vdi.pizzaapp.utils.FormatHelper
import com.vdi.pizzaapp.viewmodel.PizzaViewModel
import com.vdi.pizzaapp.utils.SharedPreferencesHelper   // 🔹 Pastikan ada di project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PizzaScreen(viewModel: PizzaViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var editingPizza by remember { mutableStateOf<Pizza?>(null) }
    val pizzaList by viewModel.pizzas.collectAsState(initial = emptyList())
    var showScanner by remember { mutableStateOf(false) }
    val cleanPrice = remember(price) {
        price.replace(Regex("[^\\d]"), "")
    }

    // 🔹 Tambahan untuk user prefs
    val prefHelper = remember { SharedPreferencesHelper(context) }
    var userName by remember { mutableStateOf(prefHelper.getUserName() ?: "") }
    var showNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pizza Dex",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5C25C),
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (showScanner) {
                QRCodeScannerScreen(
                    onResult = { result ->
                        Log.d("SCAN_RESULT", result)
                        Toast.makeText(context, "Hasil: $result", Toast.LENGTH_SHORT).show()
                        val parts = result.split(";")
                        if (parts.size == 2) {
                            name = parts[0]
                            price = parts[1]
                        }
                        showScanner = false
                    },
                    onClose = { showScanner = false }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tombol Scan dan Crash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showScanner = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFBC02D),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Scan QR")
                }

                Button(
                    onClick = { throw RuntimeException("Test Crash") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFAB91),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Crash", color = MaterialTheme.colorScheme.onError)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1) // background card kuning pucat
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Input Nama Pizza
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Pizza") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Harga
                    OutlinedTextField(
                        value = price,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                price = newValue
                            }
                        },
                        label = { Text("Harga (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            if (cleanPrice.isNotEmpty()) {
                                Text("Preview: ${FormatHelper.toRupiah(cleanPrice.toDoubleOrNull() ?: 0.0)}")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tombol Tambah / Update
                    Button(
                        onClick = {
                            when {
                                name.isBlank() -> {
                                    Toast.makeText(context, "Nama pizza tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                }
                                price.isBlank() -> {
                                    Toast.makeText(context, "Harga pizza tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                }
                                price.toDoubleOrNull() == null -> {
                                    Toast.makeText(context, "Harga harus berupa angka", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    val priceValue = price.toDouble()
                                    val newPizza = editingPizza?.copy(name = name, price = priceValue)
                                        ?: Pizza(name = name, price = priceValue)

                                    if (editingPizza != null) {
                                        viewModel.update(newPizza)
                                        Toast.makeText(context, "Pizza berhasil diupdate", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.insert(newPizza)
                                        Toast.makeText(context, "Pizza berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    }

                                    name = ""
                                    price = ""
                                    editingPizza = null
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFBC02D),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(if (editingPizza != null) "Update Pizza" else "Tambah Pizza")
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1)
                )
            ) {
                Column {
                    // Header dengan counter
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color =Color(0xFFFFF8E1),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp, 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Pizza",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFC107)
                            ) {
                                Text(
                                    text = "${pizzaList.size}",
                                    modifier = Modifier.padding(12.dp, 6.dp),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Content
                    if (pizzaList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Belum ada pizza yang ditambahkan",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pizzaList) { pizza ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            name = pizza.name
                                            price = pizza.price.toInt().toString()
                                            editingPizza = pizza
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (editingPizza?.id == pizza.id)
                                            Color(0xFFFFF59D)
                                        else Color(0xFFFFFFFF)

                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (editingPizza?.id == pizza.id) 4.dp else 1.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = pizza.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = FormatHelper.toRupiah(pizza.price),
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.delete(pizza)
                                                if (editingPizza?.id == pizza.id) {
                                                    Toast.makeText(
                                                        context,
                                                        "Pizza ${pizza.name} dihapus",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Hapus Pizza",
                                                tint = Color.Red
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ================== Pengaturan Nama User ==================
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pengaturan Pengguna",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showNameDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF90CAF9),
                    contentColor = Color.Black
                )
            ) {
                Text(if (userName.isNotEmpty()) "Ubah Nama User" else "Masukkan Nama User")
            }

            if (userName.isNotEmpty()) {
                Text(
                    text = "Halo, $userName!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (showNameDialog) {
                AlertDialog(
                    onDismissRequest = { showNameDialog = false },
                    title = { Text("Masukkan Nama Anda") },
                    text = {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Nama User") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                prefHelper.saveUserName(userName)
                                Toast.makeText(context, "Nama berhasil disimpan", Toast.LENGTH_SHORT).show()
                                showNameDialog = false
                            }
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNameDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}
