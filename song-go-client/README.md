# Song GO Client

## Quick Start

### 1. Getting the program.

a) If your computer is running Linux or MacOS, you can just download a pre-built binary from [here](https://artifacts.oicr.on.ca/artifactory/webapp/#/artifacts/browse/tree/General/overture-binaries/0.0.1/linux_64/song-client). Pick the version that fits your operating system, download it, and move it into a directory where you want to keep it. Make sure that directory is listed in your PATH environment variable; if it isn't, add it.

Try typing `song-client help`; if it gives you a screen full of help text, then the program is working. Your installation is done!

b) If you're running operating system that isn't Linux or MacOS, you can
still try out song-client; but you'll need to compile it yourself.`

Step 1: First install a Go compiler that works with your operating system and
computer architecture. Look [here](http://golang.org/doc/install) for the "Getting Started" page that describes the Go compilers that are available for various popular operating systems, and how to install and run them. Follow their
instructions carefully!

Step 2: Test your Go installation. Make sure that you can compile and run an example Go program, so that you know that your Go compiler is working, and that
you can successfully run programs that it creates.

Step 3: Once you know your compiler works, just type `go get github/overture-stack/SONG/song-go-client`, and your go compiler will download and build the song-client program for you directly from our github repository.

Step 4: Try typing `song-client help`; if it gives you a screen full of help text, then the program is working! Your installation is done!

### 2. Configuration

Next, you need to tell your song-client which _SONG server_ you want to connect
to, which _study_ you want to upload data for, and provide a valid _authentication token_, to prove to the SONG server that you're allowed to make changes
to the study you've specified.

From a command line, run:
`song-client configure`. It will show you the current configuration
settings, as well as the name of the file they are stored in.

You can edit that file with any text editor; or you can just type
`song-client configure --edit`, and then fill in the values when prompted.

To run the song client, you need:

1. The **url** of the SONG server that you want to upload to.
2. The **name** of the study that you're uploading data for.
3. An **access token** from the authentication service that your SONG server is
   using. This is essentially a password that the system uses to know that you're
   authorized to modify the study; like all passwords, keep it private, and don't
   share it!

If you want to submit data to multiple SONG servers or multiple studies on the
same SONG server, you can over-ride all of these settings on the command line.

See `song help configure` for full details; see `song-client help` for
a full list of all the commands you can run, or check out the examples below.

The output of the song-client is JSON; and one program that is often useful
for processing JSON data is called 'jq'. You can find it [here](https://stedolan.github.io/jq/).

Some of the examples use it; but you don't need it. 'jq' produces nicely formatted, coloured JSON output; and lets you pick specific fields in the JSON output
to display. Without it, the JSON is harder to read, and you won't be able to
limit the JSON output to only the field you want; you'll see everything.

You can install 'jq' on your system, or you can run any of the examples without it; you just may have to look through more JSON output fields than just the one
the example is talking about. To run the examples without 'jq', just stop
typing when you see '| jq'; don't type the '| jq', or anything else; just
hit <Enter>.

### 3. Examples

You can find the all of the example files in the example directory, located
in the same directory as this README.

#### Stage 1: SONG Submit

1. Check that the song server is running

   ```
   song-client status --ping
   ```

2. Submit the example VariantCall payload, which contains the metadata. The response will contain the `analysisId`

   ```
   song-client submit ./example/exampleVariantCall.json
   ```

3. Search for the submitted analysis, and observe the field `analysisState` is set to `UNPUBLISHED`
   ```
   song-client search -a <analysisId>
   ```

#### Stage 2: Song Manifest Generation

1. Generate a manifest for the `icgc-storage-client` in [Stage 3](#stage-3-icgc-storage-upload)
   ```
   sudo song-client manifest <analysisId> manifest.txt
   ```

#### Stage 3: ICGC-Storage Upload

Upload the manifest file to `icgc-dcc-storage` server using the [icgc-storage-client](http://docs.icgc.org/software/binaries/#storage-client). This will upload the files specified in the [exampleVariantCall.json](https://github.com/overture-stack/SONG/blob/develop/docker/example/exampleVariantCall.json) payload, which are located in the `./example` directory

```
icgc-storage-client upload --manifest manifest.txt
```

#### Stage 4: SONG Publish

1. Using the same `analysisId` as before, publish it. Essentially, this is the handshake between the metadata stored in the SONG server (via the analysisIds) and the files stored in the `icgc-storage-server` (the files described by the `analysisId`). If there are any missing files listed on the manifest that have not
   been uploaded to the storage server, you will get an error when you try to
   publish listing the files that still need to be uploaded.

   ```
   song-client publish <analysisId>
   ```

2. Search the `analysisId`, pipe it to jq and filter for `analysisState`, and observe the analysis has successfully been **published** \!\!\!
   ```
   song-client search -a <analysisId> |  jq ‘.analysisState’
   ```

### License

Copyright (c) 2018 The Ontario Institute for Cancer Research. All rights
reserved.

This program and the accompanying materials are made available under the
terms of the GNU Public License v3.0. You should have received a copy of
the GNU General Public License along with
this program. If not, see <http://www.gnu.org/licenses/>.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING,BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
