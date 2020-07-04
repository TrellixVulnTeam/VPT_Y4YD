#pragma once
#include <SDL.h>
#include <SDL_image.h>
#include <iostream>
#include <string>
#include <SDL_ttf.h>
#include <vector>
#include "projects/VPT/Background.h"
#include "projects/VPT/Border.h"
#include <algorithm>
#include "projects/VPT/RelativePaths.h"
#include <functional>
using namespace std;
class AppObject
{
public:
	AppObject() { tint = SDL_Color{ 0, 0, 0, 0 }; };
	void PreInit(string img_path);
	void BasicInit(SDL_Renderer* renderer, int w, int h, int x, int y);
	virtual void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	virtual void collide(int CollisionVal);
	virtual void draw();
	virtual void update();
	virtual void input(SDL_Event e);
	virtual void ChangeImage(string img_path);
	virtual void ApplyEffects();
	SDL_Rect* getBounds();
	string image_path;
	SDL_Texture* texture;
	SDL_Renderer* renderer_m;
	SDL_Rect* srcR, destR;
	int width, height;
	int x_m, y_m;
	int id;
	SDL_Color tint;
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
	int textsize_m;
	int text_w, text_h;
	SDL_Surface* tmpsurface;
	int textds, textde;
};

class Button : public AppObject {
public:
	Button(string img_path, string hovered_img_path, Text* text, int text_w, int text_h, int x_offset, int y_offset, void(*onclick)());
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
	void(*onclick_m)();
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
	int pxToTextPos(int pxPos);
	void drawSelection(int ref1, int ref2);
	int textToPxPos(int textPos);
	void append(const char* c);
	void append(char c);
	void bksp();
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
	int cursorPos;
	Uint32 lastKeyPress;
	bool mouseDown;
	int selectionStart, selectionEnd;
};

class TextBox : public AppObject {
public:
	TextBox(string font_path, int textsize, int x_offset, int y_offset, string text);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void draw();
	void update();
	int x_offset_m;
	int y_offset_m;
	Text* text_m;
	string font_path_m;
	int textsize_m;
	string message;
};

class Overlay : public AppObject {
public:
	Overlay(string font, string text, SDL_Color bacgroundColor, SDL_Color textColor, int textsize, int x_offset, int y_offset, Uint32 displayTime, vector<AppObject*>* overlays);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void Init(SDL_Renderer* renderer, int x, int y);
	void Init(int windowWidth, SDL_Renderer* renderer, int y);
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

class CheckBox : public AppObject{
public:
	CheckBox(string font, int textsize, int x_offset, int y_offset, string backround_img);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void draw();
	void AddCheckBox(string text);
	void input(SDL_Event e);
	void update();
	void collide(int CollisionVal);
	string font_m;
	int textsize_m;
	vector <Button*> checkboxes;
	int x_offset_m;
	int y_offset_m;
	string backround_img_m;
	void onclick() {};
};

class SimpleButton : public AppObject {
public:
	SimpleButton(Text* text, int x_offset, int y_offset, function<void()> onclick, 
		Background* background = new SolidBackground(SDL_Color{ 210, 255, 255, 255 }), Border* border = new SolidBorder(SDL_Color{0, 0, 0, 255}, 5),
		SDL_Color hoverTint = SDL_Color{255, 255, 255, 50}, SDL_Color clickTint = SDL_Color{ 255, 255, 255, 100 });
	void collide(int CollisionVal);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void Init(SDL_Renderer* renderer, int x, int y);
	void draw();
	void input(SDL_Event e);
	Background* background_m;
	Border* border_m;
	SDL_Color hoverTint_m, clickTint_m;
	int CollisionVal_m;
	Text* text_m;
	int text_w_m;
	int text_h_m;
	int x_offset_m;
	int y_offset_m;
	bool isCollided, isMouseDown;
	function<void()> onclick_m;
};

class LoadingSymbol : public AppObject {
public:
	LoadingSymbol(double rotationSpeed = 0.35, SDL_Color color = SDL_Color{ 0, 0, 0, 255 }, string imagePath = "loading.png");
	LoadingSymbol(SDL_Color color, double rotationSpeed = 0.5, string imagePath = "loading.png") : LoadingSymbol(rotationSpeed, color, imagePath) {};
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void draw();
	double rotationSpeed_m;
	SDL_Color color_m;
};
