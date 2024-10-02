#!/bin/bash
#Sync code from master

git fetch origin master
git checkout origin/master -- src/main
git checkout origin/master -- img

#copy resources
rm -rf resources
mv src/main/resources resources
echo "Copy resources success!"

#copy java src
mv src/main/java tempsrc
rm -rf src
mv tempsrc src
echo "Copy src success!"


# Create lib directory to store jar files
mkdir -p lib

# Fetch the build.gradle.kts file content from the GitHub repository
curl -s https://raw.githubusercontent.com/Liubsyy/JarEditor/master/build.gradle.kts | \
grep 'implementation("' | \
awk -F '[()]' '{print $2}' | \
sed 's/"//g' | \
while read -r dependency; do
    if [ -n "$dependency" ]; then
        # Split the dependency into group:name:version
        IFS=":" read -r group name version <<< "$dependency"

        # Replace dots in the group with slashes to construct the Maven repository path
        group_path=$(echo "$group" | tr '.' '/')

        # Construct the download URL for the jar file
        jar_url="https://repo1.maven.org/maven2/$group_path/$name/$version/$name-$version.jar"

        # Download the jar file to the lib directory
        echo "Downloading $jar_url..."
        curl -o "lib/$name-$version.jar" "$jar_url"
    fi
done








