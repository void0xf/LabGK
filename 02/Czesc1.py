import pygame
import math

pygame.init()

SCREEN_WIDTH, SCREEN_HEIGHT = 600, 600
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))

WHITE = (255, 255, 255)
BLACK = (0, 0, 0)

angle = 0
scale = 1.0
is_trapezoid = False
italic = False
italic_1 = False
mirror = False
mirror_1 = False
center_y = SCREEN_HEIGHT // 2
center_x = SCREEN_WIDTH // 2

def draw_polygon(surface, color, vertices):
    pygame.draw.polygon(surface, color, vertices)

def get_polygon_vertices(center, radius, sides, rotation=0, scale=1.0, is_trapezoid=False, italic=False, italic_1=False, mirror=False, mirror_1=False):
    vertices = []
    for i in range(sides):
        angle = math.radians(rotation + (360 / sides) * i)
        if is_trapezoid and i % 2 == 0:
            offset = radius * 0.2
        else:
            offset = 0
        x = center[0] + (radius + offset) * math.cos(angle) * scale
        y = center[1] + (radius + offset) * math.sin(angle) * scale
        if italic:
            x += (y - center[1]) * 0.3
        if italic_1:
            y += (x - center[0]) * 0.3
        if mirror:
            x = 2 * center[0] - x
        if mirror_1:
            y = center[1] - (y - center[1])
        vertices.append((x, y))
    return vertices

running = True
while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
        elif event.type == pygame.KEYDOWN:
            if event.key == pygame.K_2:
                angle += 45
                scale = 1.2
                is_trapezoid = False
            elif event.key == pygame.K_3:
                scale = 1
                angle += 90
                is_trapezoid = False
            elif event.key == pygame.K_4:
                italic = not italic
            elif event.key == pygame.K_5:
                scale = 1.5
                center_y = 50
            elif event.key == pygame.K_6:
                italic = not italic
                italic_1 = not italic_1
                center_y = 300
            elif event.key == pygame.K_7:
                angle += 45
                scale = 1.5
            elif event.key == pygame.K_8:
                mirror = not mirror
                center_y = 450
                angle += 30
            elif event.key == pygame.K_9:
                center_y = 300
                italic = not italic
                angle += 30
                center_x = SCREEN_WIDTH - 150

    screen.fill(WHITE)
    vertices = get_polygon_vertices((center_x, center_y), 150, 12, angle, scale, is_trapezoid, italic, mirror, mirror_1, italic_1)
    draw_polygon(screen, BLACK, vertices)
    pygame.display.flip()
    pygame.time.wait(10)

pygame.quit()
