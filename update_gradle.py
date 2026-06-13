with open('app\\build.gradle.kts', 'r') as f:
    content = f.read()
content = content.replace(
    'kapt("androidx.room:room-compiler:2.8.4")',
    'implementation("androidx.work:work-runtime-ktx:2.8.1")\n    kapt("androidx.room:room-compiler:2.8.4")'
)
with open('app\\build.gradle.kts', 'w') as f:
    f.write(content)
print('WorkManager dependency added to build.gradle.kts')
