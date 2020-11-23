/*
 * Copyright 2018 Google Inc. All rights reserved.
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

package ch.dominikponniah.mediaplayer.europaradio.utils

import android.app.Application
import android.content.ComponentName
import android.content.Context
import ch.dominikponniah.mediaplayer.europaradio.common.MusicServiceConnection
import ch.dominikponniah.mediaplayer.europaradio.media.MusicService
import ch.dominikponniah.mediaplayer.europaradio.viewmodels.MainActivityViewModel
import ch.dominikponniah.mediaplayer.europaradio.viewmodels.MediaItemFragmentViewModel
import ch.dominikponniah.mediaplayer.europaradio.viewmodels.NowPlayingFragmentViewModel

/**
 * Static methods used to inject classes needed for various Activities and Fragments.
 */
object InjectorUtils {
    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            context,
            ComponentName(context, MusicService::class.java)
        )
    }

    fun provideMainActivityViewModel(context: Context): MainActivityViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection =
            provideMusicServiceConnection(
                applicationContext
            )
        return MainActivityViewModel.Factory(musicServiceConnection)
    }

    fun provideMediaItemFragmentViewModel(context: Context, mediaId: String)
            : MediaItemFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection =
            provideMusicServiceConnection(
                applicationContext
            )
        return MediaItemFragmentViewModel.Factory(mediaId, musicServiceConnection)
    }

    fun provideNowPlayingFragmentViewModel(context: Context)
            : NowPlayingFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val musicServiceConnection =
            provideMusicServiceConnection(
                applicationContext
            )
        return NowPlayingFragmentViewModel.Factory(
            applicationContext as Application, musicServiceConnection
        )
    }
}
