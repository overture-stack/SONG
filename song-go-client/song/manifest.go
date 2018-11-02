/*
 *     Copyright (C) 2018  Ontario Institute for Cancer Research
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package song

import (
	"encoding/json"
	"os"
	"path/filepath"
)

type manifestFile struct {
	Info       map[string]string
	ObjectId   string
	AnalysisId string
	StudyId    string
	FileName   string
	FileSize   int64
	FileType   string
	FileAccess string
	FileMd5sum string
}

func (f *manifestFile) String() string {
	return f.ObjectId + "\t" + f.FileName + "\t" + f.FileMd5sum
}

func createManifest(analysisID string, data string, path string) string {
	var files []manifestFile

	err := json.Unmarshal([]byte(data), &files)
	if err != nil {
		panic("Couldn't convert the following JSON string to an array of manifestFile objects: '" + data + "'")
	}
	for i, _ := range files {
		absPath, err := filepath.Abs(filepath.Join(path, files[i].FileName))
		if err != nil {
			panic(err)
		}
		if _, err := os.Stat(absPath); os.IsNotExist(err) {
			panic(absPath + " does not exist")
		}
		files[i].FileName = absPath
	}

	manifest := analysisID + "\t\t\n"
	for _, f := range files {
		manifest += f.String() + "\n"
	}

	return manifest
}
