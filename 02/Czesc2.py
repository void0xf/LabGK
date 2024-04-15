import pygame

# Inicjalizacja Pygame
pygame.init()

# Ustawienia ekranu
screen_width, screen_height = 800, 600
screen = pygame.display.set_mode((screen_width, screen_height))

# Kolory
WHITE = (255, 255, 255)
GREEN = (0, 255, 0)

# Uruchomienie pętli gry
running = True
while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False

    screen.fill(WHITE)  # Czyszczenie ekranu i wypełnienie kolorem białym

    # Wierzchołki obróconego trójkąta (90 stopni w prawo)
    triangle_points = [(400, 300), (300, 400), (300, 200)]

    # Lustrzane odbicie trójkąta
    triangle_points_mirrored = [(400, 300), (500, 400), (500, 200)]

    # Rysowanie trójkątów
    pygame.draw.polygon(screen, GREEN, triangle_points)
    pygame.draw.polygon(screen, GREEN, triangle_points_mirrored)

    # Rysowanie prostokąta na górze ekranu
    pygame.draw.rect(screen, GREEN, (300, 200, 200, 100))  # Prostokąt między trójkątami

    pygame.display.flip()  # Aktualizacja zawartości okna

pygame.quit()  # Zamykanie Pygame po wyjściu z pętli
