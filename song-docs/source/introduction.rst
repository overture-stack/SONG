==============
Introduction
==============


What is SONG?
======================

SONG is a robust metadata and validation system used to quickly and reliably track genome metadata scattered across multiple Cloud storage systems. 
In the field of genomics and bioinformatics, metadata managed by simple solutions such as spreadsheets and text files require significant time and effort to maintain and ensure the data is reliable. 
With several users and thousands of genomic files, tracking the state of metadata and their associations can become a nightmare. 
The purpose of SONG is to minimize human intervention by imposing rules and structure to user uploads, which as a result produces high quality and reliable metadata with a minimal amount of effort.

.. note::

    **SONG** is a recurrsive acronym for **S**\ ONG's **O**\ ur **N**\ ew **G**\ NOS

.. 
    What SONG is NOT
    ==================

.. _introduction_features:

Features
======================

- Synchronous and asynchronous metadata validation using `JsonSchema <http://json-schema.org>`_
- Strictly enforced data relationships and fields
- Optional **info** json fields for user specific metadata
- Standard REST API that is easy to understand and work with
- Simple and fast metadata searching
- Export payloads for SONG mirroring
- Clear and concise error handling
- ACL security using OAuth2 and scopes based on study codes
- Unifies metadata with object data stored in SCORE
- Built-in `Swagger UI <https://song.cancercollaboratory.org/swagger-ui.html>`_ for API interaction


Data Submission Workflow
======================================
- 2 parts: metadata upload (song) and object data upload (score)

Projects Using SONG
======================

.. generated at https://staticmapmaker.com/google/

.. image:: https://maps.googleapis.com/maps/api/staticmap?autoscale=false&size=600x300&maptype=roadmap&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:1%7CToronto&markers=size:mid%7Ccolor:0xffb100%7Clabel:2%7CVirginia&markers=size:mid%7Ccolor:0x0a00ff%7Clabel:3%7CBerlin&markers=size:mid%7Ccolor:0x00d70b%7Clabel:4%7CHeidelberg

.. .. image:: https://maps.googleapis.com/maps/api/staticmap?autoscale=false&size=600x300&maptype=roadmap&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:1%7CToronto&markers=size:mid%7Ccolor:0xffb100%7Clabel:2%7CVirginia

.. .. image:: https://maps.googleapis.com/maps/api/staticmap?autoscale=2&size=600x300&maptype=roadmap&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:2%7CAWS+Virginia&markers=size:mid%7Ccolor:0xff0000%7Clabel:1%7CCancer+Collaboratory+Toronto

**Legend**:

.. raw:: html

    <ul style="list-style-type:none" >
        <li>
            <strong>
                <font color="red">
                    Cancer Collaboratory - Toronto
                </font>
            </strong>: <a href="https://song.cancercollaboratory.org">song.cancercollaboratory.org</a>
        </li>
        <li>
            <strong>
                <font color="orange">
                    AWS - Virginia
                </font>
            </strong>: <a href="https://virginia.song.icgc.org">virginia.song.icgc.org</a>
        </li>
        <li>
            <strong>
                <font color="blue">
                    DKFZ - Berlin
                </font>
            </strong> 
        </li>
        <li>
            <strong>
                <font color="green">
                    DKFZ - Heidelberg
                </font>
            </strong>
        </li>
    </ul>


Where to go from here?
============================
- run the whole system locally using docker (put link)
- interact with the python sdk(put link)
- play with a cli (link to clis)
- 

License
=============

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
