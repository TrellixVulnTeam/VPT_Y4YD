#pragma once
#include <SDL.h>
#include <SDL_image.h>
#include <iostream>
#include <string>
#include <SDL_ttf.h>
#include <vector>
using namespace std;
class AppObject
{
public:
	AppObject() {};
	void PreInit(const char* img_path);
	void BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y);
	virtual void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	virtual void collide(int CollisionVal);
	virtual void draw();
	virtual void update();
	virtual void input(SDL_Event e);
	virtual void ChangeImage(const char* img_path);
	const char* image_path;
	SDL_Texture* texture;
	SDL_Renderer* renderer_m;
	SDL_Rect* srcR, destR;
	int width, height;
	int x_m, y_m;
	int id;
};

class Text : public AppObject{
public:
	Text(string font, string text, SDL_Color textcolor, int textsize);
	void BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void draw();
	void update();
	void GetTextSize();
	void ChangeText(string text);
	TTF_Font* font_m;
	string font_path;
	string message;
	SDL_Texture* texture;
	SDL_Rect* srcR, destR;
	SDL_Color textcolor_m;
	SDL_Renderer* renderer_m;
	int width, height;
	int x_m, y_m;
	int textsize_m;
	int text_w, text_h;
	SDL_Surface* tmpsurface;
};

class Button : public AppObject {
public:
	Button(string img_path, string hovered_img_path, Text* text, int text_w, int text_h, int x_offset, int y_offset);
	void collide(int CollisionVal);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void draw();
	void input(SDL_Event e);
	string hovered_image_path;
	string image_path1;
	int CollisionVal_m;
	Text* text_m;
	int text_w_m;
	int text_h_m;
	int x_offset_m;
	int y_offset_m;
	SDL_Texture* normalTexture;
	SDL_Texture* pressedTexture;
	bool isCollided;
};

class TextField : public AppObject{
public:
	TextField(string placeHolderText, string font_path, int textsize, int x_offset, int y_offset, const char pwChar = 0);
	void Init(string projectDir, SDL_Renderer* renderer, int w, int h, int x, int y);
	void draw();
	void input(SDL_Event e);
	void update();
	void collide(int CollisionVal);
	void updateText();
	bool hasTextReachedBorder();
	char pwChar_m;
	Text* text_m;
	string placeHolderText_m;
	string font_path_m;
	int textsize_m;
	string message;
	int x_offset_m;
	int y_offset_m;
	int CollisionVal_m = -1;
	bool hasclicked;
	bool hasclicked_prev;
};

class Overlay : public AppObject {
public:
	Overlay(string font, string text, SDL_Color bacgroundColor, SDL_Color textColor, int textsize, int x_offset, int y_offset, Uint32 displayTime, vector<AppObject*>* overlays);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void Init(SDL_Renderer* renderer, int x, int y);
	void draw();
	void update();
	Text* text_m;
	SDL_Color bacgroundColor_m;
	int x_offset_m;
	int y_offset_m;
	Uint32 displayTime_m;
	bool hasStart;
	Uint32 startTime;
	SDL_Rect* bounds;
	vector<AppObject*>* overlays_m;
};