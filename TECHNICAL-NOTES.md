# Technical notes

## WebView startup

The native layout is drawn first. WebView creation is posted to the UI queue after the Activity content view has been attached. Network loading itself is asynchronous.

## Footer hiding

The footer is hidden by one injected CSS rule:

- semantic `footer`
- `#colophon`
- `#site-footer`
- `.site-footer`
- `.tra-footer`
- common footer widget/bottom classes
- `[role="contentinfo"]`

There is no MutationObserver and no continuous JavaScript loop. The CSS rule naturally applies to matching footer markup inserted later.

## Intro

`tra_original_logo.png` is a byte-for-byte copy of the supplied original TRA logo. Only Android view alpha/scale animation is applied to that bitmap.

## Website routes

- Home: https://www.the-retrenchment-adventures.co.za/
- Products: https://www.the-retrenchment-adventures.co.za/service-booking/
- Events: https://www.the-retrenchment-adventures.co.za/events-list-style-with-search-box/
- Gallery: https://www.the-retrenchment-adventures.co.za/gallery/
- Book: https://www.the-retrenchment-adventures.co.za/contact-us/
