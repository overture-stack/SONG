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
	"sort"
)

type InfoKey struct {
	Key   string `json:"key"`
	Value string `json:"value"`
}

type InfoSearchRequest struct {
	IncludeInfo bool      `json:"includeInfo"`
	SearchTerms []InfoKey `json:"searchTerms"`
}

func createInfoSearchRequest(includeInfo bool, terms map[string]string) InfoSearchRequest {
	var searchTerms = []InfoKey{}
	var searchKeys []string

	for k, _ := range terms {
		searchKeys = append(searchKeys, k)
	}

	sort.Strings(searchKeys)

	for _, k := range searchKeys {
		v := terms[k]
		searchTerms = append(searchTerms, InfoKey{k, v})
	}
	return InfoSearchRequest{includeInfo, searchTerms}
}

func createInfoSearchJSON(includeInfo bool, terms map[string]string) []byte {
	data := createInfoSearchRequest(includeInfo, terms)
	searchRequest, err := json.Marshal(data)

	if err != nil {
		panic(err)
	}

	return searchRequest
}
