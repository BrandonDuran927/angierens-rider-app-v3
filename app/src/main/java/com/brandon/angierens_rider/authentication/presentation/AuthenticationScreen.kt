package com.brandon.angierens_rider.authentication.presentation

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import com.brandon.angierens_rider.ui.theme.AngierensRiderTheme
import com.brandon.angierens_rider.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AuthenticationScreenCore(
    navController: NavController,
    viewModel: AuthenticationViewModel
) {
    Screen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@Composable
private fun Screen(
    state: AuthenticationState,
    onAction: (AuthenticationAction) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for image
        Box(
            modifier = Modifier
                .size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.rider_logo_authentication),
                contentDescription = "Rider Logo"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign In Title
        Text(
            text = "Sign In",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9A501E)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email TextField
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(AuthenticationAction.OnEmailChange(it)) },
            label = { Text("Name or Email Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9A501E),
                unfocusedBorderColor = Color(0xFFAC7653),
                focusedLabelColor = Color(0xFF9A501E),
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(AuthenticationAction.OnPasswordChange(it)) },
            label = { Text("Enter Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9A501E),
                unfocusedBorderColor = Color(0xFFAC7653),
                focusedLabelColor = Color(0xFF9A501E),
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                onAction(AuthenticationAction.OnLogin)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9A501E)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Login",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Password Text
        TextButton(onClick = { /* Handle reset password */ }) {
            Text(
                text = "Reset Password",
                color = Color(0xFF9A501E),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Create Account Text
        Text(
            text = "Exclusive for Angieren's Lutong Bahay Rider",
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            color = Color.Black.copy(alpha = 0.5f)
        )
    }

    if (state.error?.isNotEmpty() == true) {
        AlertDialog(
            onDismissRequest = {
                onAction(AuthenticationAction.ClearError)
            },
            title = {
                Text(text = "Login Failed", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = state.error)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(AuthenticationAction.ClearError)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9A501E))
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenPreview() {
    AngierensRiderTheme {
        Screen(
            state = AuthenticationState(),
            onAction = {}
        )
    }
}