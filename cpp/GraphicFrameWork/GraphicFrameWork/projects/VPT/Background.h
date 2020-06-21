#pragma once
#include <string>
#include <SDL.h>
#include <SDL_image.h>
using namespace std;
class Background {
public:
	virtual void draw(SDL_Renderer* renderer, const SDL_Rect* rect) = 0;
};

class SolidBackground : public Background {
public:
	SolidBackground(SDL_Color color);
	void draw(SDL_Renderer* renderer, const SDL_Rect* rect);
private:
	SDL_Color color_m;
};

class ImageBackground : public Background {
public:
	ImageBackground(string imagePath);
	void draw(SDL_Renderer* renderer, const SDL_Rect* rect);
private:
	bool textureInit;
	SDL_Texture* image_m;
	string imagePath_m;
};