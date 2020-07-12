#pragma once
#include "../../AppObject.h"
#include <string>
#include <regex>

using namespace std;

class PlaceableBounding : public AppObject {
public:
	PlaceableBounding() { };
	static string FormatBackground(Background* background);
	static string FormatBorder(Border* border);
	static string FormatColor(SDL_Color color);
	static string FormatReleventData(int numArgs, ...);
	virtual string PrintReleventData() {
		return FormatReleventData(5, "DynamicObject", to_string(x_m), to_string(y_m), to_string(width), to_string(height));
	};
	int collideid;
	int selectW;
	int selectH;
};

class PlaceableButton : public PlaceableBounding {
public:
	PlaceableButton(string image_path, string image_selected) : image_selected(image_selected) { PreInit(image_path); };
	string image_selected;
	string PrintReleventData() {
		return FormatReleventData(7, "Button", to_string(x_m), to_string(y_m), to_string(width), to_string(height), image_path, image_selected);
	};
};

class PlaceableTextBox : public PlaceableBounding {
public:
	PlaceableTextBox() {};
	string PrintReleventData() {
		return FormatReleventData(5, "TextBox", to_string(x_m), to_string(y_m), to_string(width), to_string(height));
	}
};

class PlaceableText : public PlaceableBounding {
public:
	PlaceableText();
	void Init(SDL_Renderer* renderer, int x, int y);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y) { Init(renderer, x, y); };
	string PrintReleventData() {
		return FormatReleventData(6, "Text", to_string(x_m), to_string(y_m), text->message, text->textcolor_m, text->textsize_m);
	}
	virtual void update();
	virtual void draw();
	Text* text;
};

class PlaceableTextField : public PlaceableBounding {
public:
	struct DefaultTextFieldData {
		int textsize = 30;
		int x_offset = 125;
		int y_offset = 25;
		int w = 500;
		int h = 100;
	};
	PlaceableTextField();
	void Init(SDL_Renderer* renderer, int x, int y);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y) { Init(renderer, x, y); };
	void update();
	void draw();
	int text_offset_x, text_offset_y;
	Text* placeholderText;
	char pwChar;
	string PrintReleventData() {
		return FormatReleventData(11, "TextField", to_string(x_m), to_string(y_m), to_string(width), to_string(height), image_path, to_string(text_offset_x), to_string(text_offset_y), placeholderText->message, string(1, pwChar), to_string(placeholderText->textsize_m));
	}
};

class PlaceableSimpleButton : public PlaceableBounding {
public:
	PlaceableSimpleButton();
	void Init(SDL_Renderer* renderer, int x, int y);
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y) { Init(renderer, x, y); };
	void collide(int CollisionVal);
	void update();
	void input(SDL_Event e);
	void draw();
	SimpleButton* button;
	string PrintReleventData() {
		return FormatReleventData(13, "SimpleButton", to_string(x_m), to_string(y_m), to_string(width), to_string(height), button->text_m->message, button->x_offset_m, button->y_offset_m, FormatColor(button->hoverTint_m), FormatColor(button->clickTint_m), button->background_m ? "true" : "false", FormatBackground(button->background_m), FormatBorder(button->border_m));
	}
};

class PlaceableLoadingSymbol : public PlaceableBounding {
public:
	PlaceableLoadingSymbol();
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	void update();
	void draw();
	LoadingSymbol* symbol;
	string PrintReleventData() {
		return FormatReleventData(8, "LoadingSymbol", to_string(x_m), to_string(y_m), to_string(width), to_string(height), to_string(symbol->rotationSpeed_m), FormatColor(symbol->color_m), symbol->image_path);
	}
};