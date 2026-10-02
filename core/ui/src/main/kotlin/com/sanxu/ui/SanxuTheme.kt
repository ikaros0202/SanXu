package com.sanxu.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

private val Light=lightColorScheme(primary=Color(0xFFB43C32),onPrimary=Color.White,primaryContainer=Color(0xFFF6E8E2),onPrimaryContainer=Color(0xFF6D2721),secondary=Color(0xFF536048),onSecondary=Color.White,secondaryContainer=Color(0xFFE7EBDC),onSecondaryContainer=Color(0xFF29331F),tertiary=Color(0xFF47646A),background=Color(0xFFF6F4EF),onBackground=Color(0xFF222725),surface=Color(0xFFFFFEFA),onSurface=Color(0xFF222725),surfaceVariant=Color(0xFFEEF0E8),onSurfaceVariant=Color(0xFF626A64),outline=Color(0xFF7D887F),outlineVariant=Color(0xFFDCDDD4),surfaceContainer=Color(0xFFF0F0E7),surfaceContainerLow=Color(0xFFF7F7EF),surfaceContainerHigh=Color(0xFFE9ECE3),error=Color(0xFFA22A25))
private val Dark=darkColorScheme(primary=Color(0xFFFFB4A9),onPrimary=Color(0xFF60140F),primaryContainer=Color(0xFF44302A),onPrimaryContainer=Color(0xFFFFDAD3),secondary=Color(0xFFBDCBAE),onSecondary=Color(0xFF29351F),secondaryContainer=Color(0xFF354131),onSecondaryContainer=Color(0xFFE1E9D3),tertiary=Color(0xFFA7CCD1),background=Color(0xFF151B19),onBackground=Color(0xFFEEEFE6),surface=Color(0xFF202823),onSurface=Color(0xFFEEEFE6),surfaceVariant=Color(0xFF29352D),onSurfaceVariant=Color(0xFFB9C2B9),outline=Color(0xFF8C988F),outlineVariant=Color(0xFF465148),surfaceContainer=Color(0xFF202823),surfaceContainerLow=Color(0xFF1C231E),surfaceContainerHigh=Color(0xFF29352D))
val NumberFont=FontFamily.Monospace
@Composable fun SanxuTheme(dark:Boolean=isSystemInDarkTheme(),content:@Composable ()->Unit) {
    MaterialTheme(colorScheme=if(dark) Dark else Light,shapes=Shapes(small=RoundedCornerShape(12.dp),medium=RoundedCornerShape(16.dp),large=RoundedCornerShape(24.dp)),typography=Typography(
        headlineLarge=TextStyle(fontSize=32.sp,fontWeight=FontWeight.SemiBold),headlineMedium=TextStyle(fontSize=24.sp,lineHeight=32.sp,fontWeight=FontWeight.SemiBold),
        titleLarge=TextStyle(fontSize=20.sp,fontWeight=FontWeight.SemiBold),titleMedium=TextStyle(fontSize=16.sp,fontWeight=FontWeight.SemiBold),
        bodyLarge=TextStyle(fontSize=16.sp,lineHeight=24.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=21.sp),bodySmall=TextStyle(fontSize=12.sp,lineHeight=18.sp),labelLarge=TextStyle(fontSize=14.sp,fontWeight=FontWeight.Medium)
    ),content=content)
}
@Composable fun SectionLabel(title:String,modifier:Modifier=Modifier){Text(title,modifier.padding(top=16.dp,bottom=10.dp),style=MaterialTheme.typography.titleMedium)}
@Composable fun PaperPanel(modifier:Modifier=Modifier,emphasized:Boolean=false,content:@Composable ColumnScope.()->Unit){Surface(modifier,shape=MaterialTheme.shapes.medium,color=if(emphasized) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface){Column(Modifier.padding(16.dp),content=content)}}
