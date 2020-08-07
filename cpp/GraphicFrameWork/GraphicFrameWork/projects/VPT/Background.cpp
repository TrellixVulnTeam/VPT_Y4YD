#include "Background.h"
#include "Utils.h"

SolidBackground::SolidBackground(SDL_Color color) {
	color_m = color;
}

void SolidBackground::draw(SDL_Renderer* renderer, const SDL_Rect* rect) {
	Utils::SDL_PushRendererState(renderer);
	Utils::SDL_SetRenderDrawSDLColor(renderer, color_m);
	SDL_RenderFillRect(renderer, rect);
	Utils::SDL_PopRendererState(renderer);
}

ImageBackground::ImageBackground(string imagePath) {
	imagePath_m = imagePath;
	textureInit = false;
}

void ImageBackground::draw(SDL_Renderer* renderer, const SDL_Rect* rect) {
	if (!textureInit) {
		image_m = IMG_LoadTexture(renderer, imagePath_m.c_str());
		textureInit = true;
	}
	SDL_RenderCopy(renderer, image_m, NULL, rect);
}