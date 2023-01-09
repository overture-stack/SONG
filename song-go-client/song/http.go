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
	"bytes"
	"io/ioutil"
	"net/http"
	"net/url"
)

func (c *Client) post(address url.URL, body []byte) string {
	req := createRequest("POST", address, body)

	if body != nil {
		req.Header.Add("Content-Type", "application/json")
	}

	return c.makeRequest(req)
}

func (c *Client) put(address url.URL, body []byte) string {
	req := createRequest("PUT", address, body)

	req.Header.Add("Content-Type", "application/json")
	req.Header.Add("Accept", "application/json")

	return c.makeRequest(req)
}

func (c *Client) get(address url.URL) string {
	req := createRequest("GET", address, nil)
	return c.makeRequest(req)
}

func createRequest(requestType string, address url.URL, body []byte) *http.Request {
	var req *http.Request
	var err error

	if body == nil {
		req, err = http.NewRequest(requestType, address.String(), nil)
	} else {
		req, err = http.NewRequest(requestType, address.String(), bytes.NewReader(body))
	}

	if err != nil {
		panic(err)
	}

	return req
}

func (c *Client) makeRequest(req *http.Request) string {
	req.Header.Add("Authorization", "Bearer "+c.accessToken)

	resp, err := c.httpClient.Do(req)
	if err != nil {
		panic(err)
	}

	defer resp.Body.Close()

	body, _ := ioutil.ReadAll(resp.Body)
	if resp.StatusCode != http.StatusOK {
		panic("Request was not OK: " + resp.Status + string(body))
	}

	return string(body)
}
