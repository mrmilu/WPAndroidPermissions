#WP Android Permissions

Android Marshmallow permissions made easy

Works flawlessly on:

	android.support.v7.app.AppCompatActivity extended classes
	android.support.v4.app.Fragment extended classes

###GRADLE:

	````
	repositories {
	    maven {
	        url "https://jitpack.io"
	    }
	}
	dependencies {
	        compile 'com.github.webpartners:WPAndroidPermissions:1.5'
	}
    ````

###USAGE:


1. Add annotations with the desired permissions:

	````
	@ActivityWithRuntimePermissions // at class level for Activities
	
	@FragmentWithRuntimePermissions // at class level for Fragments
	
	@HostFragmentWithPermissions // if an Activity hosts Fragment that requires permissions
	
	@NeedPermissions(Manifest.permission.YOUR_PERMISSION) // at function level
	````
	
2. Add to the invocation of your Activity/Fragments the suffix: _Generated, like:

	````
	startActivity(new Intent(MainActivity.this, ActivityWithPermission_Generated.class));
	````


3. Implement the callback interface on your classes for better permission feedback:

	````
	MyActivity implements PermissionRequestResponse
	````

###MORE INFO:

	Go to sample module
	
###TROUBLESHOOTING:

	By the time, the library is limited to the use of one single permission by activity or fragment


###LICENSE

````
The MIT License (MIT)

Copyright (c) 2015 WebPartners

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
````
