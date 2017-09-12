SING_EXE="./sing"
ROOT_LOC="song-server/src/test/resources/documents/search"
$SING_EXE upload  -f "$ROOT_LOC/testData_0.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_1.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_2.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_3.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_4.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_5.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_6.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_7.json" | cut -d '"' -f 8 | xargs ./sing save -u
$SING_EXE upload  -f "$ROOT_LOC/testData_8.json" | cut -d '"' -f 8 | xargs ./sing save -u
