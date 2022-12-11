package com.example.appone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appone.ui.theme.AppOneTheme
import java.util.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    setContent {
      AppOneTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding()
            .statusBarsPadding(), color = MaterialTheme
            .colors.background
        ) {
          var textFieldValue by remember { mutableStateOf(getString(R.string.text_hint)) }

          val startLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
          ) { it ->
            if (it.resultCode == Activity.RESULT_OK) {
              val result =
                it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
              val text = result?.get(0).toString()
              textFieldValue = text
            }
          }

          fun launchSpeechToTextService(context: Context) {
            val locale = Locale.getDefault()
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
              val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
              intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH
              )
              intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
              startLauncher.launch(intent)
            }
          }

          SourceApp(
            textFieldValue,
            onTextValueChanged = {
              textFieldValue = it
            },
            launchSpeechToText = {
              launchSpeechToTextService(this)
            },
            searchText = {
              searchOnBrowser(textFieldValue)
            }
          )
        }
      }
    }
  }

  private fun searchOnBrowser(text: String) {
    val locale = Locale.getDefault()
    val package_name = "com.android.chrome"

    val URL = "https://www.google.com/search?q=$text"
    val builder = CustomTabsIntent.Builder()
    builder.setShowTitle(true)
    builder.setInstantAppsEnabled(true)
    val customBuilder = builder.build()

    val headers = Bundle()
    headers.putString("Accept-Language", locale.toString())
    customBuilder.intent.setPackage(package_name)
      .putExtra(Browser.EXTRA_HEADERS, headers)
    // launch the web url using browser
    customBuilder.launchUrl(this, Uri.parse(URL))
  }
}


@Composable
private fun SourceApp(
  textFieldValue: String,
  onTextValueChanged: (String) -> Unit,
  launchSpeechToText: () -> Unit,
  searchText:
    () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(top = 30.dp), horizontalAlignment = Alignment
      .CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    TextField(value = textFieldValue, onValueChange = { newText ->
      onTextValueChanged(newText)
    }, placeholder = {
      Text(
        text = stringResource(id = R.string.text_hint),
        style = MaterialTheme.typography.body1
      )
    }, modifier = Modifier.height(300.dp))
    Button(onClick = { launchSpeechToText() }) {
      Text(
        text = stringResource(id = R.string.button_voice_to_text),
        style = MaterialTheme.typography.h6
      )
    }
    Button(onClick = {
      searchText()
    }) {
      Text(
        text = stringResource(id = R.string.button_search),
        style = MaterialTheme.typography.h6
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  AppOneTheme {
    SourceApp("", {}, {}) {}
  }
}