from PIL import Image

R, G, B = 225 * 2, 143 * 2, 90 * 2

IMG = "bundle.png"
OUT = "bundle_gray.png"

img = Image.open(IMG)
pixels = img.load()

"""all_cols = []

for x in range(img.width):
    for y in range(img.height):
        col = pixels[x, y]
        if(col[3] == 255):
            all_cols.append(col)

best_diff = 100000000000000000
best_col = (-1, -1, -1)

def score_color(r, g, b):
    score = 0
    for col in all_cols:
        ro, go, bo = col[:3]
        rp = ro / r * 255
        gp = go / g * 255
        bp = bo / b * 255
        gray = (rp + gp + bp) / 3
        score += (rp - gray) * (rp - gray) + (gp - gray) * (gp - gray) + (bp - gray) * (bp - gray)
    return score


for r in range(224, 256, 8):
    for g in range(144, 256, 8):
        for b in range(88, 256, 8):
            score = score_color(r, g, b)
            if score < best_diff:
                best_diff = score
                best_col = (r, g, b)
                print("New Best:", best_col)

R, G, B = best_col"""

def rebase_color(col):
    r, g, b, a = col
    return (round(r / R * 255), round(g / G * 255), round(b / B * 255), a)

for x in range(img.width):
    for y in range(img.height):
        pixels[x, y] = rebase_color(pixels[x, y])

img.save(OUT)