from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.colors import HexColor, white
from reportlab.pdfbase.pdfmetrics import stringWidth
from pathlib import Path

OUT = Path("output/pdf/hagrid-voxel-map-encoding.pdf")
OUT.parent.mkdir(parents=True, exist_ok=True)

W, H = landscape(A4)
C = canvas.Canvas(str(OUT), pagesize=(W, H))

INK = HexColor("#172033")
MUTED = HexColor("#5E6A7D")
BLUE = HexColor("#3B82F6")
BLUE_D = HexColor("#1D4ED8")
CYAN = HexColor("#DDF4FF")
ORANGE = HexColor("#F59E0B")
ORANGE_L = HexColor("#FFF3D6")
GREEN = HexColor("#10B981")
GREEN_L = HexColor("#DDF8ED")
PURPLE = HexColor("#8B5CF6")
PURPLE_L = HexColor("#EEE7FF")
GRAY = HexColor("#EEF1F5")
GRID = HexColor("#97A3B6")
BG = HexColor("#F8FAFC")
RED = HexColor("#EF4444")


def text(x, y, s, size=13, color=INK, font="Helvetica", anchor="start"):
    C.setFont(font, size)
    C.setFillColor(color)
    if anchor == "middle":
        x -= stringWidth(s, font, size) / 2
    elif anchor == "end":
        x -= stringWidth(s, font, size)
    C.drawString(x, y, s)


def wrapped(x, y, s, width, size=12, leading=16, color=MUTED, font="Helvetica"):
    words = s.split()
    lines, line = [], ""
    for word in words:
        test = word if not line else line + " " + word
        if stringWidth(test, font, size) <= width:
            line = test
        else:
            lines.append(line)
            line = word
    if line:
        lines.append(line)
    for i, line in enumerate(lines):
        text(x, y - i * leading, line, size, color, font)
    return y - len(lines) * leading


def round_box(x, y, w, h, fill=white, stroke=GRAY, radius=10, sw=1):
    C.setLineWidth(sw)
    C.setStrokeColor(stroke)
    C.setFillColor(fill)
    C.roundRect(x, y, w, h, radius, fill=1, stroke=1)


def arrow(x1, y1, x2, y2, color=BLUE, sw=2):
    C.setStrokeColor(color)
    C.setFillColor(color)
    C.setLineWidth(sw)
    C.line(x1, y1, x2, y2)
    import math
    a = math.atan2(y2-y1, x2-x1)
    for d in (-0.45, 0.45):
        C.line(x2, y2, x2-10*math.cos(a+d), y2-10*math.sin(a+d))


def header(kicker, title, subtitle):
    C.setFillColor(BG)
    C.rect(0, 0, W, H, fill=1, stroke=0)
    text(42, H-42, kicker.upper(), 9, BLUE_D, "Helvetica-Bold")
    text(42, H-75, title, 25, INK, "Helvetica-Bold")
    wrapped(42, H-99, subtitle, W-84, 11.5, 15, MUTED)


def footer(page):
    C.setStrokeColor(GRAY)
    C.setLineWidth(.6)
    C.line(42, 25, W-42, 25)
    text(42, 12, "GridAnalysis - Hagrid voxel-map encoding", 8, MUTED)
    text(W-42, 12, str(page), 8, MUTED, anchor="end")


def grid(x, y, cols, rows, size, labels, fills=None, thick=None, axes=True):
    fills = fills or {}
    for r in range(rows):
        for col in range(cols):
            bx, by = x + col*size, y + (rows-1-r)*size
            C.setFillColor(fills.get((col, r), white))
            C.setStrokeColor(GRID)
            C.setLineWidth(1)
            C.rect(bx, by, size, size, fill=1, stroke=1)
            label = labels[r][col]
            text(bx+size/2, by+size/2-5, str(label), 12, INK, "Helvetica-Bold", "middle")
    if thick:
        C.setStrokeColor(INK)
        C.setLineWidth(2.4)
        for vx in thick.get("v", []):
            C.line(x+vx*size, y, x+vx*size, y+rows*size)
        for hy in thick.get("h", []):
            C.line(x, y+hy*size, x+cols*size, y+hy*size)
    C.setStrokeColor(INK)
    C.setLineWidth(1.8)
    C.rect(x, y, cols*size, rows*size, fill=0, stroke=1)
    if axes:
        for col in range(cols): text(x+(col+.5)*size, y+rows*size+9, f"x={col}", 8.5, MUTED, anchor="middle")
        for r in range(rows): text(x-9, y+(rows-r-.5)*size-3, f"y={r}", 8.5, MUTED, anchor="end")


def entry_box(x, y, idx, kind, value, highlight=False):
    fill = ORANGE_L if kind == "CHILDREN" else GREEN_L
    stroke = ORANGE if kind == "CHILDREN" else GREEN
    if highlight: stroke, fill = BLUE_D, CYAN
    round_box(x, y, 82, 70, fill, stroke, 7, 2 if highlight else 1.2)
    text(x+8, y+54, f"entries[{idx}]", 8.5, MUTED, "Helvetica-Bold")
    shown_kind = "CHILDREN AT" if kind == "CHILDREN" else kind
    shown_value = f"[{value}]" if kind == "CHILDREN" else str(value)
    text(x+41, y+33, shown_kind, 8.5, stroke, "Helvetica-Bold", "middle")
    text(x+41, y+13, shown_value, 15, INK, "Helvetica-Bold", "middle")


# Page 1
header("Step 1", "Begin with the familiar 2 x 2 uniform grid",
       "At this point, a voxel and a cell are the same thing: each grid square owns its own primitive-reference list.")
grid(75, 120, 2, 2, 145, [["Cell 0", "Cell 1"], ["Cell 2", "Cell 3"]],
     {(0,0): CYAN, (1,0): ORANGE_L, (0,1): GREEN_L, (1,1): PURPLE_L})
round_box(420, 145, 360, 245)
text(445, 360, "Ordinary grid lookup", 17, INK, "Helvetica-Bold")
text(445, 320, "index = x + y x width", 16, BLUE_D, "Courier-Bold")
wrapped(445, 280, "For voxel (0, 1): index = 0 + 1 x 2 = 2, so the lookup returns Cell 2.", 300, 13, 19, INK)
text(445, 220, "Cell 2", 15, INK, "Helvetica-Bold")
text(520, 220, "min=(0,1), max=(1,2)", 12, MUTED, "Courier")
text(520, 194, "references=[...]", 12, MUTED, "Courier")
round_box(445, 155, 300, 25, CYAN, BLUE)
text(595, 163, "No hierarchy yet", 10, BLUE_D, "Helvetica-Bold", "middle")
footer(1); C.showPage()

# Page 2
header("Step 2", "Only the top-right region needs more detail",
       "We split that one top-level square into four smaller cells. The other three regions stay large.")
labels = [["C0","C0","C3","C4"],["C0","C0","C5","C6"],["C1","C1","C2","C2"],["C1","C1","C2","C2"]]
fills = {}
palette = {"C0":CYAN,"C1":ORANGE_L,"C2":GREEN_L,"C3":PURPLE_L,"C4":HexColor("#FFE4E6"),"C5":HexColor("#E2F7E8"),"C6":HexColor("#E9EAFD")}
for r,row in enumerate(labels):
    for col,v in enumerate(row): fills[(col,r)] = palette[v]
grid(75, 105, 4, 4, 76, labels, fills, {"v":[2],"h":[2]})
text(227, 425, "finest virtual coordinates", 11, BLUE_D, "Helvetica-Bold", "middle")
round_box(430, 130, 350, 245)
text(455, 345, "What actually changed?", 17, INK, "Helvetica-Bold")
text(460, 304, "Top-level grid", 11, MUTED)
text(735, 304, "2 x 2", 14, INK, "Helvetica-Bold", "end")
text(460, 267, "Finest coordinate lattice", 11, MUTED)
text(735, 267, "4 x 4", 14, INK, "Helvetica-Bold", "end")
text(460, 230, "Final compact cells", 11, MUTED)
text(735, 230, "7", 14, INK, "Helvetica-Bold", "end")
C.setStrokeColor(GRAY); C.line(455,215,755,215)
wrapped(455, 192, "Build keeps coarse leaves first (C0-C2), then child leaves (C3-C6). The 4 x 4 lattice is only a shared ruler.", 295, 11.5, 16, INK)
footer(2); C.showPage()

# Page 3
header("Step 3", "The whole scene gets a finer integer ruler",
       "Subdivision introduces half-size boundaries. Hagrid scales the common coordinate system instead of storing fractional bounds.")
round_box(55, 128, 330, 260)
text(220, 400, "Before subdivision", 16, INK, "Helvetica-Bold", "middle")
grid(105, 165, 2, 2, 105, [["T0","T1"],["T2","T3"]], axes=False)
text(220, 140, "temporary top cells T0-T3", 11, MUTED, "Courier", "middle")
arrow(405, 260, 455, 260, BLUE_D, 3)
text(430, 280, "scale x2", 10, BLUE_D, "Helvetica-Bold", "middle")
round_box(475, 128, 310, 260)
text(630, 400, "After subdivision + compaction", 15, INK, "Helvetica-Bold", "middle")
grid(520, 165, 4, 4, 55, labels, fills, {"v":[2],"h":[2]}, axes=False)
text(630, 140, "boundaries: 0, 1, 2, 3, 4", 12, MUTED, "Courier", "middle")
round_box(150, 75, 545, 34, CYAN, BLUE)
text(422, 86, "Large cells simply span several virtual voxels.", 12, BLUE_D, "Helvetica-Bold", "middle")
footer(3); C.showPage()

# Page 4
header("Step 4", "Store the nested grids in one flat entry array",
       "Every entry says either STOP at a cell, or continue in a smaller grid whose entries begin elsewhere in the array.")
text(62, 400, "Top-level 2 x 2", 14, INK, "Helvetica-Bold")
grid(62, 205, 2, 2, 92, [["C0","MORE"],["C1","C2"]],
     {(0,0):GREEN_L,(1,0):ORANGE_L,(0,1):GREEN_L,(1,1):GREEN_L}, axes=False)
arrow(254, 343, 355, 343, ORANGE, 2.5)
text(305, 359, "top-right only", 9, ORANGE, "Helvetica-Bold", "middle")
text(365, 420, "Its child 2 x 2", 14, INK, "Helvetica-Bold")
grid(365, 270, 2, 2, 65, [["C3","C4"],["C5","C6"]],
     {(0,0):ORANGE_L,(1,0):GREEN_L,(0,1):PURPLE_L,(1,1):HexColor("#FFE4E6")}, axes=False)
text(62, 170, "Contiguous entries[] array", 14, INK, "Helvetica-Bold")
xs = [62+i*91 for i in range(8)]
vals = [("CELL",0),("CHILDREN",4),("CELL",1),("CELL",2),("CELL",3),("CELL",4),("CELL",5),("CELL",6)]
for i,(kind,val) in enumerate(vals): entry_box(xs[i], 75, i, kind, val)
C.setStrokeColor(ORANGE); C.setLineWidth(2)
C.line(xs[1]+41, 145, xs[1]+41, 160); C.line(xs[1]+41,160,xs[4]+41,160); C.line(xs[4]+41,160,xs[4]+41,148)
text((xs[1]+xs[4]+82)/2, 164, "begin = 4", 9, ORANGE, "Helvetica-Bold", "middle")
footer(4); C.showPage()

# Page 5
header("Step 5", "Trace lookup A: voxel (0, 3)",
       "This voxel lies in a large, unsubdivided region, so the lookup ends after one array access.")
highlight = {(0,3):HexColor("#FDE68A")}
f2 = dict(fills); f2.update(highlight)
grid(60, 115, 4, 4, 70, labels, f2, {"v":[2],"h":[2]})
text(200, 417, "voxel (0,3)", 12, RED, "Helvetica-Bold", "middle")
arrow(105, 120, 105, 160, RED, 2)
round_box(390, 130, 390, 250)
text(415, 350, "1. Choose top-level square", 14, INK, "Helvetica-Bold")
text(430, 316, "topX = 0 / 2 = 0", 12, MUTED, "Courier")
text(430, 294, "topY = 3 / 2 = 1", 12, MUTED, "Courier")
text(430, 264, "topIndex = 0 + 1 x 2 = 2", 12, BLUE_D, "Courier-Bold")
text(415, 225, "2. Read entries[2]", 14, INK, "Helvetica-Bold")
entry_box(430, 140, 2, "CELL", 1, True)
text(535, 171, "STOP", 18, GREEN, "Helvetica-Bold")
text(605, 171, "Result: Cell 1", 14, INK, "Helvetica-Bold")
footer(5); C.showPage()

# Page 6
header("Step 6", "Trace lookup B: voxel (3, 1)",
       "This voxel lands in the refined top-right region, so one entry redirects the lookup to its four children.")
f3 = dict(fills); f3[(3,1)] = HexColor("#FDE68A")
grid(50, 150, 4, 4, 58, labels, f3, {"v":[2],"h":[2]})
arrow(253, 244, 253, 276, RED, 2)
text(166, 420, "voxel (3,1)", 12, RED, "Helvetica-Bold", "middle")
C.setFillColor(BLUE_D); C.circle(330,394,11,fill=1,stroke=0)
text(330, 390, "A", 12, white, "Helvetica-Bold", "middle")
text(355, 390, "Top square: (3/2, 1/2) = (1,0)", 12, INK)
entry_box(355, 292, 1, "CHILDREN", 4, True)
arrow(440, 326, 500, 326, BLUE_D, 2)
text(520, 334, "children start at 4", 11, BLUE_D, "Helvetica-Bold")
C.setFillColor(PURPLE); C.circle(330,252,11,fill=1,stroke=0)
text(330, 248, "B", 12, white, "Helvetica-Bold", "middle")
text(355, 248, "Local square: (3%2, 1%2) = (1,1)", 12, INK)
text(355, 218, "child offset = 1 + 1 x 2 = 3", 12, MUTED, "Courier")
text(355, 190, "child index = 4 + 3 = 7", 12, BLUE_D, "Courier-Bold")
entry_box(560, 150, 7, "CELL", 6, True)
text(660, 181, "STOP -> Cell 6", 15, GREEN, "Helvetica-Bold")
footer(6); C.showPage()

# Page 7
header("Step 7", "Refine C6 one more time",
       "C6 is the bottom-right first-level region. In final compaction, six earlier leaves precede its four second-level leaves, so they receive C6-C9.")
text(55, 402, "Before: 4 x 4 virtual lattice", 15, INK, "Helvetica-Bold")
grid(55, 135, 4, 4, 62, labels, fills, {"v":[2],"h":[2]}, axes=False)
C.setStrokeColor(RED); C.setLineWidth(4); C.rect(55+3*62,135+2*62,62,62,fill=0,stroke=1)
text(55+3.5*62, 115, "refine C6", 11, RED, "Helvetica-Bold", "middle")
arrow(330, 260, 410, 260, BLUE_D, 3)
text(370, 280, "scale x2", 10, BLUE_D, "Helvetica-Bold", "middle")

labels8 = []
for r in range(8):
    row=[]
    for col in range(8):
        if r < 4 and col < 4: v="C0"
        elif r < 2 and 4 <= col < 6: v="C3"
        elif r < 2 and col >= 6: v="C4"
        elif 2 <= r < 4 and 4 <= col < 6: v="C5"
        elif r == 2 and col == 6: v="C6"
        elif r == 2 and col == 7: v="C7"
        elif r == 3 and col == 6: v="C8"
        elif r == 3 and col == 7: v="C9"
        elif r >= 4 and col < 4: v="C1"
        else: v="C2"
        row.append(v)
    labels8.append(row)
palette8 = dict(palette)
palette8.update({"C4":HexColor("#FDE68A"),"C5":HexColor("#FED7AA"),"C6":HexColor("#FBCFE8"),"C7":HexColor("#CCFBF1"),"C8":HexColor("#E2F7E8"),"C9":HexColor("#E9EAFD")})
fills8={(col,r):palette8[labels8[r][col]] for r in range(8) for col in range(8)}
text(455, 402, "After: 8 x 8 virtual lattice", 15, INK, "Helvetica-Bold")
grid(455, 125, 8, 8, 34, labels8, fills8, {"v":[4,6],"h":[4,6]}, axes=False)
round_box(160, 70, 520, 33, CYAN, BLUE)
text(420, 81, "Prefix-sum order: 6 earlier leaves, then the four new leaves C6-C9.", 11, BLUE_D, "Helvetica-Bold", "middle")
footer(7); C.showPage()

# Page 8
header("Step 8", "Read the complete 8 x 8 ownership map",
       "The repeated labels show which fine virtual voxels belong to each actual cell. Thick lines mark the nested 2 x 2 subdivision boundaries.")
grid(52, 92, 8, 8, 48, labels8, fills8, {"v":[4,6],"h":[4,6]})
round_box(480, 125, 310, 270, white, GRID)
text(505, 360, "Cell bounds in virtual coordinates", 15, INK, "Helvetica-Bold")
bounds = [
    "C0  min=(0,0)  max=(4,4)",
    "C1  min=(0,4)  max=(4,8)",
    "C2  min=(4,4)  max=(8,8)",
    "C3  min=(4,0)  max=(6,2)",
    "C4  min=(6,0)  max=(8,2)",
    "C5  min=(4,2)  max=(6,4)",
    "C6  min=(6,2)  max=(7,3)",
    "C7  min=(7,2)  max=(8,3)",
    "C8  min=(6,3)  max=(7,4)",
    "C9  min=(7,3)  max=(8,4)",
]
for i,s in enumerate(bounds): text(505, 330-i*22, s, 9.5, MUTED, "Courier")
text(635, 105, "10 actual cells, not 64", 13, GREEN, "Helvetica-Bold", "middle")
footer(8); C.showPage()

# Page 9
header("Step 9", "Three nested 2 x 2 grids become one flat array",
       "After compaction, every active cell has one consecutive ID from C0 through C9. The two CHILDREN entries are not cells.")
text(42, 405, "Top level", 13, INK, "Helvetica-Bold")
grid(42, 275, 2, 2, 58, [["C0","MORE"],["C1","C2"]], {(0,0):GREEN_L,(1,0):ORANGE_L,(0,1):GREEN_L,(1,1):GREEN_L}, axes=False)
arrow(165, 333, 225, 333, ORANGE, 2)
text(225, 405, "First children", 13, INK, "Helvetica-Bold")
grid(225, 275, 2, 2, 58, [["C3","C4"],["C5","MORE"]], {(0,0):GREEN_L,(1,0):GREEN_L,(0,1):GREEN_L,(1,1):ORANGE_L}, axes=False)
arrow(348, 300, 408, 300, ORANGE, 2)
text(408, 405, "Second children", 13, INK, "Helvetica-Bold")
grid(408, 275, 2, 2, 58, [["C6","C7"],["C8","C9"]], {(0,0):GREEN_L,(1,0):GREEN_L,(0,1):GREEN_L,(1,1):GREEN_L}, axes=False)
round_box(570, 270, 220, 125, CYAN, BLUE)
text(680, 360, "Pointers", 13, BLUE_D, "Helvetica-Bold", "middle")
text(590, 327, "entries[1] -> begin 4", 10.5, INK, "Courier-Bold")
text(590, 300, "entries[7] -> begin 8", 10.5, INK, "Courier-Bold")

text(42, 235, "entries[]", 14, INK, "Helvetica-Bold")
vals12=[("CELL",0),("CHILDREN",4),("CELL",1),("CELL",2),("CELL",3),("CELL",4),("CELL",5),("CHILDREN",8),("CELL",6),("CELL",7),("CELL",8),("CELL",9)]
for i,(kind,val) in enumerate(vals12):
    row=i//6; col=i%6
    entry_box(42+col*126, 145-row*82, i, kind, val, highlight=(i in (1,7)))
footer(9); C.showPage()

# Page 10
header("Step 10", "Trace voxel (7, 3) through both child levels",
       "The top level uses division only. Each child level divides to its scale, then uses modulo 2 to select its local position.")
grid(42, 130, 8, 8, 36, labels8, {**fills8,(7,3):HexColor("#FDE68A")}, {"v":[4,6],"h":[4,6]})
round_box(395, 100, 400, 320, white, GRID)
steps=[
    ("A  Top level", "(7/4, 3/4) = (1,0)", "entries[1] -> children begin 4"),
    ("B  First child", "((7/2)%2, (3/2)%2) = (1,1)", "offset 3 -> entries[7] -> begin 8"),
    ("C  Second child", "(7%2, 3%2) = (1,1)", "offset 3 -> entries[11] -> Cell 9"),
]
for i,(name,formula,result) in enumerate(steps):
    yy=370-i*92
    text(420,yy,name,13,INK,"Helvetica-Bold")
    text(430,yy-30,formula,10.5,BLUE_D,"Courier-Bold")
    text(430,yy-56,result,10.5,MUTED,"Courier")
    if i<2: C.setStrokeColor(GRAY); C.line(415,yy-70,775,yy-70)
round_box(460, 62, 270, 30, GREEN_L, GREEN)
text(595, 72, "voxel (7,3) -> Cell 9", 12, GREEN, "Helvetica-Bold", "middle")
footer(10); C.showPage()

# Page 11
header("Step 11", "The storage saving grows with depth",
       "A dense lookup table reserves one ownership entry for every finest voxel. The hierarchy stores only top-level entries plus children for refined regions.")
round_box(55, 155, 330, 220, white, GRID)
text(220, 335, "Dense ownership map", 16, INK, "Helvetica-Bold", "middle")
text(220, 270, "8 x 8", 34, RED, "Helvetica-Bold", "middle")
text(220, 225, "64 map entries", 20, RED, "Helvetica-Bold", "middle")
text(220, 185, "even where ownership repeats", 11, MUTED, anchor="middle")
round_box(455, 155, 330, 220, white, GRID)
text(620, 335, "Hierarchical ownership map", 16, INK, "Helvetica-Bold", "middle")
text(490, 285, "4", 22, BLUE_D, "Helvetica-Bold"); text(535,290,"top entries",11,INK)
text(490, 245, "+ 4", 22, ORANGE, "Helvetica-Bold"); text(550,250,"first children",11,INK)
text(490, 205, "+ 4", 22, PURPLE, "Helvetica-Bold"); text(550,210,"second children",11,INK)
C.setStrokeColor(GRAY); C.line(485,190,755,190)
text(620, 165, "12 map entries", 18, GREEN, "Helvetica-Bold", "middle")
round_box(155, 88, 530, 36, GREEN_L, GREEN)
text(420, 100, "64 dense entries -> 12 hierarchical entries -> 10 actual cells", 12, GREEN, "Helvetica-Bold", "middle")
footer(11); C.showPage()

# Page 12
header("The mental model", "A fine address maps to an adaptive cell",
       "The cells still have min/max bounds and primitive references. The voxel map is only the fast directory that finds the owning cell.")
round_box(50, 240, 180, 115, CYAN, BLUE)
text(140, 322, "FINE VOXEL", 12, BLUE_D, "Helvetica-Bold", "middle")
text(140, 282, "(x, y)", 24, INK, "Courier-Bold", "middle")
arrow(235, 297, 315, 297, BLUE_D, 3)
round_box(320, 220, 200, 155, ORANGE_L, ORANGE)
text(420, 337, "VOXEL MAP", 12, ORANGE, "Helvetica-Bold", "middle")
text(420, 299, "CELL", 16, INK, "Helvetica-Bold", "middle")
text(420, 270, "or", 11, MUTED, anchor="middle")
text(420, 242, "MORE -> children", 13, INK, "Helvetica-Bold", "middle")
arrow(525, 297, 605, 297, BLUE_D, 3)
round_box(610, 220, 180, 155, GREEN_L, GREEN)
text(700, 337, "OWNING CELL", 12, GREEN, "Helvetica-Bold", "middle")
text(700, 301, "min / max", 14, INK, "Courier-Bold", "middle")
text(700, 272, "begin / end", 14, INK, "Courier-Bold", "middle")
text(700, 243, "primitive refs", 12, MUTED, "Helvetica-Bold", "middle")
round_box(105, 105, 630, 66, white, GRID)
text(420, 143, "Subdivision increases global coordinate precision", 15, INK, "Helvetica-Bold", "middle")
text(420, 119, "but allocates new map entries only where finer detail is needed.", 12, MUTED, anchor="middle")
footer(12); C.showPage()

C.save()
print(OUT.resolve())
