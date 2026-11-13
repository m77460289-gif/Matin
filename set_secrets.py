import os, sys, base64, json, urllib.request
from nacl import encoding, public

REPO = 'm77460289-gif/Matin'
token = os.environ.get('GH_TOKEN')
if not token:
    print('NO_TOKEN'); sys.exit(1)

headers = {
    'Authorization': 'Bearer ' + token,
    'Accept': 'application/vnd.github+json',
    'User-Agent': 'Mozilla/5.0',
    'X-GitHub-Api-Version': '2022-11-28',
}

def api(method, path, data=None):
    url = 'https://api.github.com' + path
    body = json.dumps(data).encode() if data is not None else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    if body is not None:
        req.add_header('Content-Type', 'application/json')
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.loads(r.read().decode() or '{}')

pk = api('GET', '/repos/' + REPO + '/actions/secrets/public-key')
key_id = pk['key_id']
pub_key = public.PublicKey(pk['key'].encode(), encoding.Base64Encoder())
print('PUBLIC_KEY_OK', key_id)

with open('release-keystore.b64') as f:
    keystore_b64 = f.read().strip()

secrets = {
    'RELEASE_KEYSTORE_BASE64': keystore_b64,
    'RELEASE_STORE_PASSWORD': 'Konkur2025',
    'RELEASE_KEY_PASSWORD': 'Konkur2025',
    'RELEASE_KEY_ALIAS': 'konkur',
}

sealed_box = public.SealedBox(pub_key)
for name, value in secrets.items():
    enc = base64.b64encode(sealed_box.encrypt(value.encode())).decode()
    api('PUT', '/repos/' + REPO + '/actions/secrets/' + name, {'encrypted_value': enc, 'key_id': key_id})
    print('SET_OK', name, 'len=' + str(len(value)))
print('ALL_DONE')
