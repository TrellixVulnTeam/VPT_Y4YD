#pragma once
#include <SDL.h>
#include <map>
#include <vector>
#include <Windows.h>
#include <string>
#include "ResultId.h"
#include <jni.h>
#include <locale>
#include <codecvt>

using namespace std;
class Utils {
public:
	struct SDL_Dimension {
		int width;
		int height;
	};
	struct SDL_RendererState {
		SDL_Color color;
	};
	static void SDL_PushRendererState(SDL_Renderer* renderer);
	static void SDL_PopRendererState(SDL_Renderer* renderer);
	static SDL_RendererState SDL_GetRendererState(SDL_Renderer* renderer);
	static void SDL_SetRendererState(SDL_Renderer* renderer, SDL_RendererState state);
	static SDL_Color SDL_GetRenderDrawSDLColor(SDL_Renderer* renderer);
	static void SDL_SetRenderDrawSDLColor(SDL_Renderer* renderer, SDL_Color color);
	static string readClipboard();
	static void writeClipboard(string data);
	template<typename kt, typename vt>
	static bool contains(map<kt, vt> m, kt key) {
		return m.count(key) == 1;
	}
	template<typename kt, typename vt>
	static vt get(map<kt, vt> m, kt key) {
		auto ittr = m.find(key);
		if (ittr == m.end()) {
			return NULL;
		}
		return ittr->second;
	}
	static bool ValidatePacketType(int packetType, int expectedType);
	static SDL_Texture* CreateTextureFromImage(jobject image, SDL_Renderer* renderer, JNIEnv* env);
	static bool contains(SDL_Rect* rect, SDL_Point point);
	static wstring FromString(string str);
	static wchar_t* UnConstWChar(const wchar_t* chars);
};