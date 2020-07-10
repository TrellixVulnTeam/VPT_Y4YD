#pragma once
#include "../../AppObject.h"
#include <string>
#include <regex>

using namespace std;

class PlaceableBounding : public AppObject {
public:
	PlaceableBounding() { };
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
	PlaceableButton() { };
	string PrintReleventData() {
		return FormatReleventData(5, "Button", to_string(x_m), to_string(y_m), to_string(width), to_string(height));
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
	void Init(SDL_Renderer* renderer, int w, int h, int x, int y);
	string PrintReleventData() {
		return FormatReleventData(5, "Text", to_string(x_m), to_string(y_m), text->message, text->textcolor_m, text->textsize_m);
	}
	virtual void update();
	virtual void draw();
	Text* text;
};

class PlaceableTextField : public PlaceableBounding {

	string PrintReleventData() {
		return FormatReleventData(5, "TextField", to_string(x_m), to_string(y_m), to_string(width), to_string(height));
	}
};

class PlaceableSimpleButton : public PlaceableBounding {

	string PrintReleventData() {
		return FormatReleventData(5, "SimpleButton", to_string(x_m), to_string(y_m), to_string(width), to_string(height));
	}
};

class PlaceableLoadingSymbol : public PlaceableBounding {

	string PrintReleventData() {
		return FormatReleventData(5, "LoadingSymbol", to_string(x_m), to_string(y_m), to_string(width), to_string(height));
	}
};