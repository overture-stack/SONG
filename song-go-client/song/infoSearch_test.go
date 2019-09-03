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

func TestInfoSearchRequest(t *testing.T) {
	x := map[string]string{"search1": "one", "search2": "two"}
	y := createInfoSearchRequest(true, x)
	z := InfoSearchRequest{true, []InfoKey{InfoKey{"search1", "one"}, InfoKey{"search2", "two"}}}

	assert.Equal(t, y, z, "Info search request (true)")

	x = map[string]string{"a": "1", "b": "2"}
	y = createInfoSearchRequest(false, x)
	z = InfoSearchRequest{false, []InfoKey{InfoKey{"a", "1"}, InfoKey{"b", "2"}}}

	assert.Equal(t, y, z, "Info search request (false)")
}
