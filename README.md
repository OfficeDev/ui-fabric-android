![Build status](https://onedrive.visualstudio.com/Design/_apis/build/status/fabric-mobile/fabric-android-CI-github?branchName=master)
[![GitHub release](https://img.shields.io/github/release/officedev/ui-fabric-android.svg)](https://github.com/OfficeDev/ui-fabric-android/releases/latest)

# [Office UI Fabric for Android](http://dev.office.com/fabric)

##### The Android UI framework for building experiences for Office and Office 365.

Office UI Fabric for Android is a native library that provides the Office UI experience for the Android platform. It contains information about colors and typography, as well as custom controls and customizations for platform controls, all from the official Fluent design language used in Office and Office 365 products.

## Contents

- [Install and use Office UI Fabric](#install-and-use-office-ui-fabric)
- [Colors and typography](#colors-and-typography)
- [Controls](#controls)
- [Demo app](#demo-app)
- [Contributing](#contributing)
- [License](#license)
- [Changelog](#changelog)

## Install and use Office UI Fabric

### 1. Requirements

API 19 or higher

### 2. Using Gradle

2.1. Inside the dependency block in your build.gradle, add this line for the OfficeUIFabric library:
```gradle
dependencies {
    ...
    implementation 'com.microsoft.uifabric:OfficeUIFabric:$version'
    ... 
}
```

2.2. Our library is published through JCenter, so make sure the `jcenter()` repository has been added to your project level build.gradle file (which usually is automatic).

2.3. Make sure you replace `$version` with the latest version of OfficeUIFabric.

### 3. Using Maven

3.1 Add the OfficeUIFabric library as a dependency:
```xml
<dependency>
  <groupId>com.microsoft.uifabric</groupId>
  <artifactId>OfficeUIFabric</artifactId>
  <version>${version}</version>
</dependency>
```

3.2. Make sure you replace `${version}` with the latest version of OfficeUIFabric.

### 4. Manual installation

4.1. Download the latest changes from the [Office UI Fabric Android](https://github.com/OfficeDev/UI-Fabric-Android) repository.

4.2. Follow [these instructions](https://developer.android.com/studio/projects/android-library) to build and output an AAR file from the OfficeUIFabric module, import the module to your project, and add it as a dependency. If you're having trouble generating an AAR file for the module, make sure you select it and run "Make Module 'OfficeUIFabric'" from the Build menu.

### 5. Import and use the library

5.1 In code:
```kotlin
import com.microsoft.officeuifabric.persona.AvatarView
```

5.2 In XML:
```xml
<com.microsoft.officeuifabric.persona.AvatarView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:name="Mona Kane" />
```

## Colors and typography

Office UI Fabric for Android provides [colors](https://github.com/OfficeDev/ui-fabric-android/blob/master/OfficeUIFabric/src/main/res/values/colors.xml) and [typography](https://github.com/OfficeDev/ui-fabric-android/blob/master/OfficeUIFabric/src/main/res/values/styles_font.xml) based on the Fluent design language.

## Controls

Office UI Fabric for Android includes an expanding library of controls written in Kotlin. These controls implement the Fluent design language and bring consistency across Office app experiences.
See the [Office UI Fabric Demo app](https://github.com/OfficeDev/ui-fabric-android/tree/master/OfficeUIFabric.Demo/src/main/java/com/microsoft/officeuifabricdemo/demos) for the latest list of controls, which include:

- AvatarView
- CalendarView
- DateTimePickerDialog
- PeoplePickerView
- PersonaChipView
- PersonaListView
- PersonaView
- Snackbar
- TemplateView
- Tooltip

## Demo app

To see samples of all of our implemented controls and design language, run the [OfficeUIFabric.Demo](https://github.com/OfficeDev/ui-fabric-android/tree/master/OfficeUIFabric.Demo) module in Android Studio.

## Contributing

Post bug reports, feature requests, and questions in [Issues](https://github.com/OfficeDev/UI-Fabric-Android/issues).

## Changelog

We use [GitHub Releases](https://github.com/blog/1547-release-your-software) to manage our releases, including the changelog between every release. You'll find a complete list of additions, fixes, and changes on the [Releases page](https://github.com/OfficeDev/UI-Fabric-Android/releases).

## License

All files on the Office UI Fabric for Android GitHub repository are subject to the MIT license. Please read the [LICENSE](https://github.com/OfficeDev/ui-fabric-android/blob/master/LICENSE) file at the root of the project.