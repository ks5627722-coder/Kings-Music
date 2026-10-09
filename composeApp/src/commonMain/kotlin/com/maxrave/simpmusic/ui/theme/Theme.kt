val colorScheme =
    wallpaperScheme
        ?: rememberDynamicColorScheme(
            seedColor = seedColor,
            isDark = isDark,
            isAmoled = false, // Pure black ki jagah custom premium background use karne ke liye false kiya
            style = PaletteStyle.TonalSpot,
            modifyColorScheme = { cs ->
                if (isDark) {
                    // Dark mode ke liye custom colors
                    if (currentTheme == "LUXE_GOLDEN") {
                        cs.copy(
                            background = luxeBackground,
                            surface = luxeSurface,
                            surfaceContainer = luxeSurface,
                            surfaceContainerHigh = luxeSurface,
                            primary = luxePrimary
                        )
                    } else if (currentTheme == "CHRONO_GRAPHITE") {
                        cs.copy(
                            background = graphiteBackground,
                            surface = graphiteSurface,
                            surfaceContainer = graphiteSurface,
                            surfaceContainerHigh = graphiteSurface,
                            primary = graphitePrimary
                        )
                    } else {
                        cs
                    }
                } else {
                    cs.withNeutralLightSurfaces() // Light mode ke liye pehle jaisa hi rakha
                }
            },
        )