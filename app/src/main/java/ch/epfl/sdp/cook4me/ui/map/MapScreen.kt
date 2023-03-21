package ch.epfl.sdp.cook4me.ui.map

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfl.sdp.cook4me.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

const val ZOOM_DEFAULT_VALUE = 15f
data class MarkerData(
    val position: LatLng,
    val title: String,
    val description: String
)

val dummyMarkers = listOf(
    MarkerData(
        position = Locations.SATELLITE,
        title = "Satellite EPFL",
        description = "EPFL satellite campus"
    ),
    MarkerData(
        position = Locations.ROLEX_LEARNING_CENTER,
        title = "EPFL Rolex Learning Center",
        description = "EPFL library and learning center"
    ),
    MarkerData(
        position = Locations.AGE_POLY,
        title = "UNIL AgePoly",
        description = "UNIL science and research building"
    )
)

@Composable
fun GoogleMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapLoaded: () -> Unit = {},
    content: @Composable () -> Unit = {},
    markers: List<MarkerData> = emptyList()
) {
    var uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false)) }
    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }
    var onClickUniversity = {
        uniLocation: LatLng ->
        cameraPositionState.position = CameraPosition.fromLatLngZoom(uniLocation, ZOOM_DEFAULT_VALUE)
    }
    var selectedMarkerTitle by remember { mutableStateOf("") }
    var mapVisible by remember { mutableStateOf(true) }
    if (mapVisible) {
        GoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = {
                onMapLoaded.invoke()
            },
        ) {
            markers.forEach { marker ->
                val markerState = rememberMarkerState(position = marker.position)
                MarkerInfoWindowContent(
                    state = markerState,
                    title = marker.title,
                    onClick = {
                        selectedMarkerTitle = marker.title
                        false
                    },
                    tag = marker.title,
                ) {
                    Text(marker.description)
                }
            }
            content()
        }
    }
    Column {
        MapTypeControls(
            onMapTypeClick = {
                Log.d("GoogleMap", "Selected map type $it")
                mapProperties = mapProperties.copy(mapType = it)
            }
        )
        Row {
            MapButton(
                text = stringResource(R.string.EPFL),
                onClick = { onClickUniversity(Locations.EPFL) }
            )
            MapButton(
                text = stringResource(R.string.UNIL),
                onClick = { onClickUniversity(Locations.UNIL) }
            )
        }
    }
}

@Composable
private fun MapTypeControls(
    onMapTypeClick: (MapType) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(state = ScrollState(0)),
        horizontalArrangement = Arrangement.Center
    ) {
        MapType.values().forEach {
            MapTypeButton(type = it) { onMapTypeClick(it) }
        }
    }
}

@Composable
private fun MapTypeButton(type: MapType, onClick: () -> Unit) =
    MapButton(text = type.toString(), onClick = onClick)

@Composable
private fun MapButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.onPrimary,
            contentColor = MaterialTheme.colors.primary
        ),
        onClick = onClick
    ) {
        Text(text = text, style = MaterialTheme.typography.body1)
    }
}

@Preview
@Composable
fun GoogleMapViewPreview() {
    GoogleMapView(
        modifier = Modifier.fillMaxSize(),
        markers = dummyMarkers
    )
}
