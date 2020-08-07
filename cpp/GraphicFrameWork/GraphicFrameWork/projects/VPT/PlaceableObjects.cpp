#include "PlaceableObjects.h"

string PlaceableBounding::FormatBackground(Background* background) {
	if (SolidBackground* sb = dynamic_cast<SolidBackground*>(background)) {
		return FormatColor(sb->color_m);
	}
	else if (ImageBackground* ib = dynamic_cast<ImageBackground*>(background)) {
		return ib->imagePath_m;
	}
	return "";
}

string PlaceableBounding::FormatBorder(Border* border) {
	if (SolidBorder* sb = dynamic_cast<SolidBorder*>(border)) {
		return "SDL_Color{ " + FormatColor(sb->color_m) + " }, " + to_string(sb->borderWidth_m);
	}
	return "";
}

string PlaceableBounding::FormatColor(SDL_Color color) {
	string sep = ", ";
	string out;
	out.append(to_string(color.r));
	out.append(sep);
	out.append(to_string(color.g));
	out.append(sep);
	out.append(to_string(color.b));
	out.append(sep);
	out.append(to_string(color.a));
	return out;
}

string PlaceableBounding::FormatReleventData(int numArgs, ...) {
	string out;
	va_list vArgs;
	va_start(vArgs, numArgs);
	for (int i = 0; i < numArgs; i++) {
		string arg = va_arg(vArgs, string);
		arg = regex_replace(arg, regex("\\\\"), "\\\\");
		arg = regex_replace(arg, regex("\""), "\\\"");
		out += "\"";
		out += arg;
		out += "\",";
	}
	va_end(vArgs);
	return out.substr(0, out.length() - 1);
}

PlaceableText::PlaceableText() {
	text = new Text(fontPath, "Text", SDL_Color{ 0, 0, 0, 255 }, 100);
}

void PlaceableText::Init(SDL_Renderer* renderer, int x, int y) {
	text->Init(renderer, 0, 0, x, y);
	BasicInit(renderer, text->width, text->width, x, y);
}

void PlaceableText::draw() {
	text->draw();
}

void PlaceableText::update() {
	text->update();
	width = text->width;
	height = text->height;
}

PlaceableTextField::PlaceableTextField() {
	DefaultTextFieldData dtfd;
	text_offset_x = dtfd.x_offset;
	text_offset_y = dtfd.y_offset;
	placeholderText = new Text(fontPath, "Placeholder", SDL_Color{ 0, 0, 0, 255}, dtfd.textsize);
	pwChar = 0;
}

void PlaceableTextField::Init(SDL_Renderer* renderer, int x, int y) {
	DefaultTextFieldData dtfd;
	string temp = dir + "..\\..\\textbox1.png";
	PreInit(temp.c_str());
	BasicInit(renderer, dtfd.w, dtfd.w, x, y);
	placeholderText->Init(renderer, 0, 0, x, y);
}

void PlaceableTextField::update() {
	placeholderText->update();
}

void PlaceableTextField::draw() {
	placeholderText->draw();
}

PlaceableSimpleButton::PlaceableSimpleButton() {
	button = new SimpleButton(new Text(fontPath, "Text", SDL_Color{ 0, 0, 0, 255 }, 40), 10, 10, [](SimpleButton* button) {});
}

void PlaceableSimpleButton::Init(SDL_Renderer* renderer, int x, int y) {
	button->Init(renderer, x, y);
	BasicInit(renderer, button->width, button->height, x, y);
}

void PlaceableSimpleButton::collide(int CollisionVal) {
	button->collide(CollisionVal);
}

void PlaceableSimpleButton::update() {
	button->update();
	width = button->width;
	height = button->height;
}

void PlaceableSimpleButton::input(SDL_Event e) {
	button->input(e);
}

void PlaceableSimpleButton::draw() {
	button->draw();
}

PlaceableLoadingSymbol::PlaceableLoadingSymbol() {
	symbol = new LoadingSymbol();
}

void PlaceableLoadingSymbol::Init(SDL_Renderer* renderer, int w, int h, int x, int y) {
	BasicInit(renderer, w, h, x, y);
	symbol->Init(renderer, w, h, x, y);
}

void PlaceableLoadingSymbol::update() {
	symbol->update();
}

void PlaceableLoadingSymbol::draw() {
	symbol->draw();
}