#include "editorHelper.h"

void EditorHelper::Init(const char* window_title, int w, int h) {
	BasicInit(window_title, w, h);

	AddObjectButton("Dynamic Object");
	AddObjectButton("Text");
	AddObjectButton("Button");
	AddObjectButton("Text Field");
	AddObjectButton("Simple Button");
	AddObjectButton("Loading Symbol");

	int buttonGridWidth = 2;
	int maxWidth = 0;
	int col = 0, nextX = 0, rowY = 0, nextY = 0;
	for (AppObject* object : AppObjects) {
		object->x_m = nextX;
		object->y_m = nextY;
		col++;
		nextX += object->width;
		maxWidth = max(maxWidth, nextX);
		nextY = max(nextY, rowY + object->height);
		if (col == buttonGridWidth) {
			nextX = 0;
			rowY = nextY;
		}
	}

	SDL_SetWindowSize(Win, maxWidth, nextY);

	SDL_DisplayMode dm;
	SDL_GetCurrentDisplayMode(0, &dm);
	int screenWidth = dm.w;
	int width, editorWidth;
	SDL_GetWindowSize(Win, &width, NULL);
	SDL_GetWindowSize(editor->Win, &editorWidth, NULL);
	int editorY;
	SDL_GetWindowPosition(editor->Win, NULL, &editorY);
	int x = (screenWidth - width - width - editorWidth) / 2;
	int editorX = (screenWidth + (4 * width) - editorWidth) / 2;
	SDL_SetWindowPosition(Win, x, editorY);
	SDL_SetWindowPosition(editor->Win, editorX, editorY);

	for (unsigned int i = 0; i < AppObjects.size(); i++) {
		AppObjects[i]->id = i;
		cm.AttachComponent(new CollisionBox(AppObjects), AppObjects[i]);
	}
}

void EditorHelper::HandleButtonInput(SimpleButton* button) {
	editor->message_m = button->text_m->message;
}

void EditorHelper::AddObjectButton(string text) {
	SimpleButton* button = new SimpleButton(new Text(fontPath, text, SDL_Color{ 0, 0, 0,255 }, 50), 10, 10, [this](SimpleButton* button) { HandleButtonInput(button); });
	button->Init(renderer, 0, 0);
	AppObjects.push_back(button);
}