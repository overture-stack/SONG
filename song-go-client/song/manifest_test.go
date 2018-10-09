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
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestManfiest(t *testing.T) {
	analysisId := "AN123"
	json := `[
  {
    "info": {
      "randomFile1Field": "someFile1Value"
    },
    "objectId": "c5066ab9-15be-5995-b73c-499c0635a6d5",
    "analysisId": "063ab3a9-1328-11e8-b46b-dbb21fb18563",
    "fileName": "myFilename1.txt",
    "studyId": "ABC123",
    "fileSize": 1234561,
    "fileType": "VCF",
    "fileMd5sum": "myMd51",
    "fileAccess": "controlled"
  },
  {
    "info": {
      "randomFile2Field": "someFile2Value"
    },
    "objectId": "01888719-1949-5406-90a0-5ccba98d9a4a",
    "analysisId": "063ab3a9-1328-11e8-b46b-dbb21fb18563",
    "fileName": "myFilename2.txt",
    "studyId": "ABC123",
    "fileSize": 1234562,
    "fileType": "VCF",
    "fileMd5sum": "myMd52",
    "fileAccess": "controlled"
  }
]`
	x := createManifest(analysisId, json)
	y := "AN123\t\t\n" + "c5066ab9-15be-5995-b73c-499c0635a6d5\tmyFilename1.txt\tmyMd51\n" + "01888719-1949-5406-90a0-5ccba98d9a4a\tmyFilename2.txt\tmyMd52\n"
	assert.Equal(t, x, y, "createManifest()")
}
