package com.meusremedios

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application root do app. Habilita o grafo de dependências do Hilt para todas
 * as camadas (ui / domain / data / notifications).
 */
@HiltAndroidApp
class MeusRemediosApplication : Application()
