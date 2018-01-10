
import unittest

import song.client


class SongTests(unittest.TestCase):

    def test_is_alive(self):
        self.assertEqual('foo'.upper(), 'FOO')

    def test_somehting(self):
        url = 'https://song.cancercollaboratory.org'
        client = song.client(url, 'BRCA-EU', '33532cea-df61-49e1-82f3-0ec48bb6bfbd', debug=True )
        self.assertEqual(client.get_server_url(), url)



if __name__ == '__main__':
    unittest.main()
