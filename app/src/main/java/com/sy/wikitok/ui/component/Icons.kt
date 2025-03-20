package com.sy.wikitok.ui.component

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @author Yeung
 * @date 2025/3/21
 *
 * Copied from material3-icons-extend
 */

public val IconHome: ImageVector
    get() {
        if (_home != null) {
            return _home!!
        }
        _home = materialIcon(name = "Filled.Home") {
            materialPath {
                moveTo(10.0f, 20.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(5.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(3.0f)
                lineTo(12.0f, 3.0f)
                lineTo(2.0f, 12.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(8.0f)
                close()
            }
        }
        return _home!!
    }

private var _home: ImageVector? = null

public val IconFavorite: ImageVector
    get() {
        if (_favorite != null) {
            return _favorite!!
        }
        _favorite = materialIcon(name = "Filled.Favorite") {
            materialPath {
                moveTo(12.0f, 21.35f)
                lineToRelative(-1.45f, -1.32f)
                curveTo(5.4f, 15.36f, 2.0f, 12.28f, 2.0f, 8.5f)
                curveTo(2.0f, 5.42f, 4.42f, 3.0f, 7.5f, 3.0f)
                curveToRelative(1.74f, 0.0f, 3.41f, 0.81f, 4.5f, 2.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3.0f, 16.5f, 3.0f)
                curveTo(19.58f, 3.0f, 22.0f, 5.42f, 22.0f, 8.5f)
                curveToRelative(0.0f, 3.78f, -3.4f, 6.86f, -8.55f, 11.54f)
                lineTo(12.0f, 21.35f)
                close()
            }
        }
        return _favorite!!
    }

private var _favorite: ImageVector? = null