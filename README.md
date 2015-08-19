#WP Android Permissions

Android Marshmallow permissions made easy

Works flawlessly on:

	````
	android.support.v7.app.AppCompatActivity extended classes
	android.support.v4.app.Fragment extended classes
	````

###USAGE:

1. Gradle

	````
	repositories {
	    maven {
	        url "https://jitpack.io"
	    }
	}
	dependencies {
	        compile 'com.github.webpartners:WPAndroidPermissions:1.0'
	}
    ````

2. Get the instance on your onCreate or onCreateView

	````
	PermissionParser.getInstance().parse(this);
	````

3. Add annotations with the desired permissions

	````
	@HasRuntimePermissions //at class level
	
	@NeedPermissions(Manifest.permission.YOUR_PERMISSION) // at function level
	````

4. Implement the callback interface on your classes (AppCompatActivities or Fragments)

	````
	MyActivity implements PermissionRequestResponse
	````

5. Add to your Activity (or in the Activity you added your Fragment)

	````
	@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (this instanceof PermissionRequestResponse)
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ((PermissionRequestResponse)this).permissionAllowed();
            else
                ((PermissionRequestResponse)this).permissionDenied();
    }
    ````


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