#pragma once
#include "SDL.h"
#include "Utils.h"
class Border {
public:
	virtual void draw(SDL_Renderer* renderer, const SDL_Rect* rect) = 0;
};

class SolidBorder: public Border {
public:
	SolidBorder(SDL_Color color, int borderWidth);
	void draw(SDL_Renderer* renderer, const SDL_Rect* rect);
	SDL_Color color_m;
	int borderWidth_m;
};