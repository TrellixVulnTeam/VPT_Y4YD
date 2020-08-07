#include "Border.h"

SolidBorder::SolidBorder(SDL_Color color, int borderWidth) {
	color_m = color;
	borderWidth_m = borderWidth;
}

void SolidBorder::draw(SDL_Renderer* renderer, const SDL_Rect* rect) {
	Utils::SDL_PushRendererState(renderer);
	Utils::SDL_SetRenderDrawSDLColor(renderer, color_m);
	SDL_RenderFillRect(renderer, new SDL_Rect{rect->x, rect->y, borderWidth_m, rect->h});
	SDL_RenderFillRect(renderer, new SDL_Rect{rect->x+rect->w-borderWidth_m, rect->y, borderWidth_m, rect->h});
	SDL_RenderFillRect(renderer, new SDL_Rect{ rect->x, rect->y, rect->w, borderWidth_m });
	SDL_RenderFillRect(renderer, new SDL_Rect{rect->x, rect->y+rect->h-borderWidth_m, rect->w, borderWidth_m});
	Utils::SDL_PopRendererState(renderer);
}