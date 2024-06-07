var express = require('express');
var router = express.Router();
var imageController = require('../controllers/imageController.js');
var multer = require('multer');
var upload = multer({dest: 'public/images'});

/*
 * GET
 */
router.get('/', imageController.list);
/*
 * GET
 */
router.get('/:id', imageController.show);
router.get('/event/:eventId', imageController.getByEvent);
/*
 * POST
 */
router.post('/', upload.single('file'), imageController.create);

/*
 * PUT
 */
router.put('/:id', imageController.update);

/*
 * DELETE
 */
router.delete('/:id', imageController.remove);

module.exports = router;
