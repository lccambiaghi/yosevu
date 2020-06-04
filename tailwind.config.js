module.exports = {
  theme: {
    aspectRatio: {
      none: 0,
      square: [1, 1],
      "16/9": [16, 9],
      "4/3": [4, 3],
      "21/9": [21, 9]
    },
    extend: {}
  },
  variants: {},
  plugins: [
    require('tailwindcss-aspect-ratio'),
    require("tailwindcss-responsive-embed"),
  ]
}
