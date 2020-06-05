#include "AppObject.h"


void AppObject::PreInit(const char* img_path)
{
	image_path = img_path;
}

void AppObject::BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;
	SDL_Surface* tmpsurface;
	tmpsurface = IMG_Load(image_path);
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_FreeSurface(tmpsurface);
}

void AppObject::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	BasicInit(renderer, w, h, x, y);
}

void AppObject::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
}

void AppObject::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
}

void AppObject::ChangeImage(const char* img_path)
{
	image_path = img_path;
	SDL_Surface* tmpsurface;
	tmpsurface = IMG_Load(image_path);
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_FreeSurface(tmpsurface);
}

Text::Text(string font, string text, SDL_Color textcolor, int textsize)
{
	textsize_m = textsize;
	textcolor_m = textcolor;
	font_m = TTF_OpenFont(font.c_str(), textsize_m);
	tmpsurface = TTF_RenderText_Solid(font_m, text.c_str(), textcolor_m);
}

void Text::BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	renderer_m = renderer;
	width = w;
	height = h;
	x_m = x;
	y_m = y;
	texture = SDL_CreateTextureFromSurface(renderer_m, tmpsurface);
	SDL_QueryTexture(texture, NULL, NULL, &width, &height);
}

void Text::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	BasicInit(renderer, w, h, x, y);
}

void Text::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
}

void Text::update()
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
}

void Text::ChangeText()
{
}

Button::Button(const char* img_path, const char* hovered_img_path, Text* text, int text_w, int text_h, int x_offset, int y_offset)
{
	image_path = img_path;
	image_path1 = img_path;
	hovered_image_path = hovered_img_path;
	if (text == nullptr) {
		text_m = nullptr;
	}
	else {
		text_m = text;
		text_w_m = text_w;
		text_h_m = text_h;
		x_offset_m = x_offset;
		y_offset_m = y_offset;
	}

}

void Button::button_update(int CollisionVal)
{
	destR.h = height;
	destR.w = width;
	destR.x = x_m;
	destR.y = y_m;
	CollisionVal_m = CollisionVal;
	if (CollisionVal != -1) {
		ChangeImage(hovered_image_path);
	}
	if (CollisionVal == -1) {
		ChangeImage(image_path1);
	}
	if (text_m != nullptr) {
		text_m->update();
	}
}

void Button::Init(SDL_Renderer* renderer, int w, int h, int x, int y)
{
	BasicInit(renderer, w, h, x, y);
	if (text_m != nullptr) {
		text_m->Init(renderer_m, text_w_m, text_h_m, x_m + x_offset_m, y_m + y_offset_m);
	}
}

void Button::draw()
{
	SDL_RenderCopy(renderer_m, texture, NULL, &destR);
	if (text_m != nullptr) {
		text_m->draw();
	}
}

void Button::input(SDL_Event e)
{
	if (e.type == SDL_MOUSEBUTTONUP && CollisionVal_m != -1) {
		cout << "test" << endl;
	}
}

