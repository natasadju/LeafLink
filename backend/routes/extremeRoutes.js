var express = require('express');
var router = express.Router();
var extremeController = require('../controllers/extremeController.js');

/*
 * GET
 */
router.get('/', extremeController.list);


/*
 * GET
 */
router.get('/:id', extremeController.show);

/*
 * POST
 */
router.post('/', extremeController.create);

/*
 * PUT
 */
router.put('/:id', extremeController.update);

/*
 * DELETE
 */
router.delete('/:id', extremeController.remove);


module.exports = router;
