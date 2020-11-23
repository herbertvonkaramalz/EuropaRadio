/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.dominikponniah.mediaplayer.europaradio

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import ch.dominikponniah.mediaplayer.europaradio.fragments.MediaItemFragment
import ch.dominikponniah.mediaplayer.europaradio.media.MusicService
import ch.dominikponniah.mediaplayer.europaradio.utils.Event
import ch.dominikponniah.mediaplayer.europaradio.utils.InjectorUtils
import ch.dominikponniah.mediaplayer.europaradio.viewmodels.MainActivityViewModel
import ch.dominikponniah.mediaplayer.europaradio.R
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext


class MainActivity : AppCompatActivity() {

  private val viewModel by viewModels<MainActivityViewModel> {
    InjectorUtils.provideMainActivityViewModel(this)
  }
  private var castContext: CastContext? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize the Cast context. This is required so that the media route button can be
    // created in the AppBar
    castContext = CastContext.getSharedInstance(this)

    /* val sp: SharedPreferences =
       getSharedPreferences("disclaimer", Context.MODE_PRIVATE)
     val editor: SharedPreferences.Editor = sp.edit()

     editor.putString("key_disclaimer", name)

     editor.apply()*/

    val sharedPreference = getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
    var editor = sharedPreference.edit()
    var disclaimerShowed = sharedPreference.getString("disclaimerShowed_rel", "null")

if(disclaimerShowed === "showed") {

      val builder = AlertDialog.Builder(this)
      builder.setTitle("Rechtliche Information und Nutzungshinweis")
      builder.setMessage("Diese App ist optimiert für die Nutzung in Fahrzeugen, wurde jedoch nicht offiziell freigegeben. Die limitierte Funktionalität soll das unsichere Nutzen des Smartphones vermeiden.\n\nZudem ist diese App inoffiziell und kein Produkt der Mack Next GmbH & Co KG!\nSämtliche Medieninhalte sind urheberrechtlich geschützte Elemente der Mack Next GmbH & Co KG.\n\nSolltest Du damit nicht einverstanden sein, dass Inhalte von Mack Next genutzt werden, kannst Du auch den offiziellen Webplayer nutzen.\n\nDer Webplayer bringt jedoch nicht die Möglichkeit, Europa-Radio mit Android-Auto oder Android-Wear zu hören oder an Chromecast-Geräte zu streamen.")
      //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

      builder.setPositiveButton("Okay") { dialog, which ->
        Toast.makeText(
          applicationContext,
          "Danke, dass Du die App verwendest!", Toast.LENGTH_SHORT
        ).show()

        editor.putString("disclaimerShowed_rel", "showed")
        editor.commit()


      }

      builder.setNeutralButton("Webplayer öffnen") { dialog, which ->
        Toast.makeText(
          applicationContext,
          "Wenn Du die App nicht mehr benötigst, kannst Du diese auch löschen - das wäre aber sehr schade!",
          Toast.LENGTH_LONG
        ).show()



        val webpage: Uri = Uri.parse("https://europa.radio")
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
          startActivity(intent)
        }
      }
      builder.show();

}


    setContentView(R.layout.activity_main)

    // Since UAMP is a music player, the volume controls should adjust the music volume while
    // in the app.
    volumeControlStream = AudioManager.STREAM_MUSIC

    /**
     * Observe [MainActivityViewModel.navigateToFragment] for [Event]s that request a
     * fragment swap.
     */
    viewModel.navigateToFragment.observe(this, Observer {
      it?.getContentIfNotHandled()?.let { fragmentRequest ->
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragmentContainer, fragmentRequest.fragment, fragmentRequest.tag
        )
        if (fragmentRequest.backStack) transaction.addToBackStack(null)
        transaction.commit()
      }
    })

    /**
     * Observe changes to the [MainActivityViewModel.rootMediaId]. When the app starts,
     * and the UI connects to [MusicService], this will be updated and the app will show
     * the initial list of media items.
     */
    viewModel.rootMediaId.observe(this,
      Observer<String> { rootMediaId ->
        rootMediaId?.let { navigateToMediaItem(it) }
      })

    /**
     * Observe [MainActivityViewModel.navigateToMediaItem] for [Event]s indicating
     * the user has requested to browse to a different [MediaItemData].
     */
    viewModel.navigateToMediaItem.observe(this, Observer {
      it?.getContentIfNotHandled()?.let { mediaId ->
        navigateToMediaItem(mediaId)
      }
    })
  }

  @Override
  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    super.onCreateOptionsMenu(menu)
    menuInflater.inflate(R.menu.main_activity_menu, menu)

    /**
     * Set up a MediaRouteButton to allow the user to control the current media playback route
     */
    CastButtonFactory.setUpMediaRouteButton(this, menu,
        R.id.media_route_menu_item
    )
    return true
  }

  private fun navigateToMediaItem(mediaId: String) {
    var fragment: MediaItemFragment? = getBrowseFragment(mediaId)
    if (fragment == null) {
      fragment = MediaItemFragment.newInstance(mediaId)
      // If this is not the top level media (root), we add it to the fragment
      // back stack, so that actionbar toggle and Back will work appropriately:
      viewModel.showFragment(fragment, !isRootId(mediaId), mediaId)
    }
  }

  private fun isRootId(mediaId: String) = mediaId == viewModel.rootMediaId.value

  private fun getBrowseFragment(mediaId: String): MediaItemFragment? {
    return supportFragmentManager.findFragmentByTag(mediaId) as MediaItemFragment?
  }
}
